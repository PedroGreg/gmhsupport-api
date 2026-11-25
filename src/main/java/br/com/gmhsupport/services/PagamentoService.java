package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.request.PagamentoRequestDTO;
import br.com.gmhsupport.dto.response.OrcamentoResponseDTO;
import br.com.gmhsupport.dto.response.PagamentoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.integracao.MercadoPagoClient;
import br.com.gmhsupport.integracao.MercadoPagoException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.PagamentoRepository;
import br.com.gmhsupport.repository.StatusChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.gmhsupport.integracao.MercadoPagoClient.campo;
import static br.com.gmhsupport.integracao.MercadoPagoClient.texto;

// Pagamentos de um chamado: o PRINCIPAL (na abertura, como no iFood) e o ADICIONAL (se o técnico pedir).
// Agora pelo Mercado Pago de verdade:
//  - Pix:    a API cria o pagamento no Mercado Pago e devolve o "copia e cola" (o app desenha o QR code)
//  - Cartão: Checkout Pro. A API cria uma "preferência" e o app abre a página do Mercado Pago (link)
//  - A aprovação chega pelo webhook (POST /pagamento/webhook), pela volta do Checkout (/pagamento/retorno)
//    ou quando o cliente toca em "Já paguei / Verificar" — em todos os casos a API CONSULTA o Mercado Pago
//    (nunca confia no que o app ou o aviso dizem).
//  - Chamado cancelado depois de pago: estorno real (POST /v1/payments/{id}/refunds)
@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    private static final Pattern REFERENCIA = Pattern.compile("^gmh-pag-(\\d+)$");
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_MP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final PagamentoRepository pagamentoRepository;
    private final ChamadoRepository chamadoRepository;
    private final HistoricoChamadoService historicoChamadoService;
    private final StatusChamadoRepository statusRepository;
    private final MercadoPagoClient mp;

    @Value("${gmh.api.url-publica:https://gmhsupport-api.onrender.com}")
    private String urlPublica;
    @Value("${gmh.mp.pix-minutos:60}")
    private int pixMinutos;
    @Value("${gmh.mp.parcelas-maximo:6}")
    private int parcelasMaximo;
    // Só para testes: "Já paguei" aprova um Pix pendente (o Pix de teste do Mercado Pago não pode ser pago)
    @Value("${gmh.pagamento.simular:false}")
    private boolean simular;
    // Só para testes: e-mail de um usuário de teste do Mercado Pago como pagador
    @Value("${gmh.mp.email-pagador-teste:}")
    private String emailPagadorTeste;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ChamadoRepository chamadoRepository,
                            HistoricoChamadoService historicoChamadoService,
                            StatusChamadoRepository statusRepository,
                            MercadoPagoClient mp) {
        this.pagamentoRepository = pagamentoRepository;
        this.chamadoRepository = chamadoRepository;
        this.historicoChamadoService = historicoChamadoService;
        this.statusRepository = statusRepository;
        this.mp = mp;
    }

    // =====================================================================
    // Criação
    // =====================================================================

    // Pagamento principal, criado junto com o chamado (mesma transação: se o Mercado Pago falhar,
    // o chamado também não é aberto). O valor vem do orçamento (PrecoService): serviço + deslocamento.
    @Transactional
    public Pagamento criarNaAbertura(Chamado chamado, OrcamentoResponseDTO orcamento, MeioPagamento meio) {
        Pagamento pagamento = new Pagamento(chamado, orcamento.total());
        pagamento.setTipo(TipoPagamento.principal);
        pagamento.setValorServico(orcamento.valorServico());
        pagamento.setValorDeslocamento(orcamento.valorDeslocamento());
        pagamento = pagamentoRepository.save(pagamento); // precisa do id para a referência no Mercado Pago
        iniciarCobranca(pagamento, meio);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        chamado.getPagamentos().add(salvo);
        logger.info("Pagamento principal {} do chamado {}: {} via {}", salvo.getId(), chamado.getId(), salvo.getStatus(), meio);
        return salvo;
    }

    // Pagamento do valor adicional, criado quando o cliente aceita o pedido do técnico
    @Transactional
    public Pagamento criarAdicional(Chamado chamado, BigDecimal valor, MeioPagamento meio) {
        if (chamado.getPagamentoAdicional() != null) {
            throw new IllegalStateException("O adicional deste chamado já tem um pagamento.");
        }
        Pagamento pagamento = new Pagamento(chamado, valor.setScale(2, RoundingMode.HALF_UP));
        pagamento.setTipo(TipoPagamento.adicional);
        pagamento = pagamentoRepository.save(pagamento);
        iniciarCobranca(pagamento, meio);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        chamado.getPagamentos().add(salvo);
        return salvo;
    }

    private void iniciarCobranca(Pagamento p, MeioPagamento meio) {
        if (meio == null) throw new IllegalArgumentException("Escolha a forma de pagamento: pix ou cartao.");
        p.setMeio(meio);
        p.setForma(null);
        p.setParcelas(null);
        p.setCartaoFinal(null);
        p.setBandeira(null);
        p.setCodigoPix(null);
        p.setLinkPagamento(null);
        p.setMpPreferenciaId(null);
        p.setMpPagamentoId(null);
        p.setPixExpiraEm(null);
        p.setMpStatus(null);
        p.setMpStatusDetalhe(null);
        if (meio == MeioPagamento.pix) gerarPix(p);
        else gerarCheckout(p);
    }

    private void gerarPix(Pagamento p) {
        LocalDateTime expira = LocalDateTime.now(FUSO).plusMinutes(pixMinutos);
        p.setForma(FormaPagamento.pix);
        p.setParcelas(1);
        p.setStatus(StatusPagamento.aguardando_pix);
        p.setPixExpiraEm(expira);

        if (!mp.configurado()) {
            if (!simular) throw new MercadoPagoException("Pagamentos indisponíveis: o Mercado Pago não está configurado na API.");
            p.setCodigoPix(codigoPixFicticio(p.getValor())); // ambiente local sem Mercado Pago
            p.setMpStatus("simulado");
            return;
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("transaction_amount", p.getValor());
        corpo.put("description", descricao(p));
        corpo.put("payment_method_id", "pix");
        corpo.put("external_reference", referencia(p));
        corpo.put("date_of_expiration", expira.atZone(FUSO).format(DATA_MP));
        corpo.put("payer", pagador(p));

        Map<String, Object> r = comAviso(corpo, c -> mp.criarPagamento(c, "gmh-pix-" + p.getId() + "-" + UUID.randomUUID()));
        String copiaECola = texto(r, "point_of_interaction", "transaction_data", "qr_code");
        if (copiaECola == null) {
            throw new MercadoPagoException("O Mercado Pago não gerou o código Pix. Confira se a conta tem uma chave Pix cadastrada.");
        }
        p.setMpPagamentoId(texto(r, "id"));
        p.setCodigoPix(copiaECola);
        p.setLinkPagamento(texto(r, "point_of_interaction", "transaction_data", "ticket_url"));
        p.setMpStatus(texto(r, "status"));
        p.setMpStatusDetalhe(texto(r, "status_detail"));
    }

    private void gerarCheckout(Pagamento p) {
        p.setStatus(StatusPagamento.pendente);
        String retorno = urlPublica.trim().replaceAll("/+$", "") + "/pagamento/retorno?ref=" + referencia(p);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", "chamado-" + p.getChamado().getId() + "-" + p.getTipo());
        item.put("title", descricao(p));
        item.put("quantity", 1);
        item.put("currency_id", "BRL");
        item.put("unit_price", p.getValor());

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("items", List.of(item));
        corpo.put("payer", pagador(p));
        corpo.put("external_reference", referencia(p));
        corpo.put("back_urls", Map.of("success", retorno, "pending", retorno, "failure", retorno));
        corpo.put("auto_return", "approved");
        corpo.put("statement_descriptor", "GMHSUPPORT");
        // Sem boleto/lotérica (demoram dias); cartão de crédito em até N parcelas
        corpo.put("payment_methods", Map.of(
                "excluded_payment_types", List.of(Map.of("id", "ticket"), Map.of("id", "atm")),
                "installments", parcelasMaximo));

        Map<String, Object> r = comAviso(corpo, mp::criarPreferencia);
        String link = texto(r, "init_point");
        if (link == null) throw new MercadoPagoException("O Mercado Pago não devolveu o link de pagamento.");
        p.setMpPreferenciaId(texto(r, "id"));
        p.setLinkPagamento(link);
    }

    // =====================================================================
    // Consultas do app
    // =====================================================================

    // GET: o dono do chamado ou o técnico responsável podem ver
    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPorChamado(Long chamadoId, String email, TipoPagamento tipo) {
        Chamado chamado = buscarChamado(chamadoId);
        validarAcesso(chamado, email);
        Pagamento pagamento = pagamentoRepository.findByChamadoIdAndTipo(chamadoId, tipo)
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui pagamento " + tipo + "."));
        return new PagamentoResponseDTO(pagamento);
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorChamado(Long chamadoId, String email) {
        Chamado chamado = buscarChamado(chamadoId);
        validarAcesso(chamado, email);
        return pagamentoRepository.findByChamadoId(chamadoId).stream().map(PagamentoResponseDTO::new).toList();
    }

    // POST pagar: troca a forma de um pagamento ainda não pago, ou gera um Pix novo (o anterior expirou)
    @Transactional
    public PagamentoResponseDTO pagar(Long chamadoId, String email, PagamentoRequestDTO dto, TipoPagamento tipo) {
        Pagamento pagamento = buscarDoDono(chamadoId, email, tipo);
        // Antes de trocar: o pagamento atual pode ter sido pago agora há pouco
        sincronizar(pagamento);
        if (pagamento.getStatus() == StatusPagamento.pago) return new PagamentoResponseDTO(pagamento);
        validarPendente(pagamento);

        // Descarta o Pix anterior no Mercado Pago (se for pago depois, o webhook estorna sozinho)
        if (pagamento.getMeio() == MeioPagamento.pix && pagamento.getMpPagamentoId() != null && mp.configurado()) {
            try {
                mp.cancelar(pagamento.getMpPagamentoId());
            } catch (MercadoPagoException e) {
                logger.warn("Não foi possível cancelar o Pix {}: {}", pagamento.getMpPagamentoId(), e.getMessage());
            }
        }
        iniciarCobranca(pagamento, dto.forma());
        return new PagamentoResponseDTO(pagamentoRepository.save(pagamento));
    }

    // POST verificar ("Já paguei"): pergunta ao Mercado Pago como está o pagamento
    @Transactional
    public PagamentoResponseDTO verificar(Long chamadoId, String email, TipoPagamento tipo) {
        Pagamento pagamento = buscarDoDono(chamadoId, email, tipo);
        if (pagamento.getStatus() == StatusPagamento.pago || pagamento.getStatus() == StatusPagamento.estornado) {
            return new PagamentoResponseDTO(pagamento);
        }
        sincronizar(pagamento);

        // Modo de teste: o Pix do ambiente de testes não tem como ser pago, então aprova aqui
        if (simular && pagamento.getStatus() == StatusPagamento.aguardando_pix) {
            aprovar(pagamento, pagamento.getMpPagamentoId(), FormaPagamento.pix, 1, null, null, "simulado");
            aoConfirmar(pagamento, "Pix — teste simulado");
        }
        return new PagamentoResponseDTO(pagamento);
    }

    // Consulta o Mercado Pago e aplica o resultado (Pix: pelo id; cartão: pela referência)
    private void sincronizar(Pagamento p) {
        if (!mp.configurado() || p.getStatus() == StatusPagamento.pago) return;
        if (p.getMpPagamentoId() != null) {
            processar(mp.buscarPagamento(p.getMpPagamentoId()));
        } else if (p.getMeio() == MeioPagamento.checkout) {
            List<Map<String, Object>> pagamentosMp = mp.buscarPorReferencia(referencia(p));
            // Primeiro os aprovados (o cliente pode ter tido um cartão recusado e depois outro aprovado)
            pagamentosMp.stream().filter(m -> "approved".equals(texto(m, "status"))).findFirst()
                    .or(() -> pagamentosMp.stream().findFirst())
                    .ifPresent(this::processar);
        }
    }

    // =====================================================================
    // Avisos do Mercado Pago (webhook e volta do Checkout Pro)
    // =====================================================================

    // Webhook: o aviso só traz o id. Os dados vêm SEMPRE de uma consulta ao Mercado Pago.
    @Transactional
    public void processarAviso(String mpPagamentoId) {
        if (mpPagamentoId == null || !mpPagamentoId.matches("\\d+")) return;
        processar(mp.buscarPagamento(mpPagamentoId));
    }

    // Volta do Checkout Pro: devolve o chamado e o tipo para o app abrir a tela certa
    @Transactional
    public Optional<Pagamento> aoVoltarDoCheckout(String referencia, String mpPagamentoId) {
        if (mpPagamentoId != null && mpPagamentoId.matches("\\d+") && mp.configurado()) {
            try {
                processar(mp.buscarPagamento(mpPagamentoId));
            } catch (MercadoPagoException e) {
                logger.warn("Retorno do checkout: {}", e.getMessage());
            }
        }
        return idDaReferencia(referencia).flatMap(pagamentoRepository::findById);
    }

    // Aplica um pagamento do Mercado Pago no pagamento correspondente daqui (external_reference = gmh-pag-ID)
    private void processar(Map<String, Object> mpPag) {
        if (mpPag == null) return;
        String mpId = texto(mpPag, "id");
        String status = texto(mpPag, "status");
        String detalhe = texto(mpPag, "status_detail");
        String ref = texto(mpPag, "external_reference");
        Optional<Long> idLocal = idDaReferencia(ref);
        if (idLocal.isEmpty()) {
            logger.info("Pagamento MP {} ignorado: referência '{}' não é do GMH Support", mpId, ref);
            return;
        }

        Pagamento p = pagamentoRepository.findById(idLocal.get()).orElse(null);
        if (p == null) {
            // Pix pago depois que o chamado foi cancelado (o pagamento daqui já foi apagado): devolve o dinheiro
            if ("approved".equals(status)) estornarSemDono(mpId, "o chamado foi cancelado antes do pagamento");
            return;
        }

        switch (status == null ? "" : status) {
            case "approved" -> {
                if (p.getStatus() == StatusPagamento.pago) {
                    if (!Objects.equals(mpId, p.getMpPagamentoId())) {
                        estornarSemDono(mpId, "pagamento em dobro do chamado " + p.getChamado().getId());
                        registrar(p.getChamado().getId(), "Pagamento em dobro detectado: o valor repetido foi estornado.");
                    }
                    return;
                }
                if (p.getStatus() == StatusPagamento.estornado || chamadoEncerrado(p)) {
                    estornarSemDono(mpId, "chamado já encerrado");
                    return;
                }
                BigDecimal valorMp = valor(campo(mpPag, "transaction_amount"));
                if (valorMp == null || valorMp.compareTo(p.getValor()) != 0) {
                    logger.error("Pagamento MP {} com valor {} diferente do esperado {}", mpId, valorMp, p.getValor());
                    estornarSemDono(mpId, "valor diferente do cobrado");
                    p.setMpStatus("valor_divergente");
                    pagamentoRepository.save(p);
                    return;
                }
                String tipoMp = texto(mpPag, "payment_type_id");
                FormaPagamento forma = switch (tipoMp == null ? "" : tipoMp) {
                    case "credit_card" -> FormaPagamento.credito;
                    case "debit_card", "prepaid_card" -> FormaPagamento.debito;
                    case "bank_transfer" -> FormaPagamento.pix;
                    default -> null; // ex.: saldo da conta Mercado Pago
                };
                Integer parcelas = inteiro(campo(mpPag, "installments"));
                String finalCartao = texto(mpPag, "card", "last_four_digits");
                String bandeira = "account_money".equals(tipoMp) ? "saldo Mercado Pago" : texto(mpPag, "payment_method_id");
                aprovar(p, mpId, forma, parcelas, finalCartao, bandeira, detalhe);
                aoConfirmar(p, descreverForma(p));
            }
            case "refunded", "charged_back" -> {
                if (p.getStatus() == StatusPagamento.pago && Objects.equals(mpId, p.getMpPagamentoId())) {
                    p.setStatus(StatusPagamento.estornado);
                    p.setMpStatus(status);
                    pagamentoRepository.save(p);
                    registrar(p.getChamado().getId(), "Pagamento de R$ " + formatar(p.getValor())
                            + " devolvido ao cliente pelo Mercado Pago.");
                }
            }
            default -> {
                // pending, in_process, rejected, cancelled...: guarda para o app explicar ao cliente
                if (p.getStatus() == StatusPagamento.pago || p.getStatus() == StatusPagamento.estornado) return;
                if (p.getMeio() == MeioPagamento.pix && !Objects.equals(mpId, p.getMpPagamentoId())) return; // Pix antigo
                p.setMpStatus(status);
                p.setMpStatusDetalhe(detalhe);
                pagamentoRepository.save(p);
            }
        }
    }

    private void aprovar(Pagamento p, String mpId, FormaPagamento forma, Integer parcelas,
                         String finalCartao, String bandeira, String detalhe) {
        p.setStatus(StatusPagamento.pago);
        p.setMpPagamentoId(mpId);
        p.setForma(forma);
        p.setParcelas(parcelas != null ? parcelas : 1);
        p.setCartaoFinal(finalCartao);
        p.setBandeira(bandeira);
        p.setMpStatus("approved");
        p.setMpStatusDetalhe(detalhe);
        p.setDataPagamento(LocalDateTime.now());
        pagamentoRepository.save(p);
        logger.info("Pagamento {} ({}) do chamado {} aprovado (MP {})", p.getId(), p.getTipo(), p.getChamado().getId(), mpId);
    }

    private void estornarSemDono(String mpId, String motivo) {
        try {
            mp.estornar(mpId);
            logger.warn("Pagamento MP {} estornado automaticamente: {}", mpId, motivo);
        } catch (MercadoPagoException e) {
            logger.error("ATENÇÃO: estorne manualmente o pagamento MP {} no painel ({}): {}", mpId, motivo, e.getMessage());
        }
    }

    // =====================================================================
    // Cancelamento
    // =====================================================================

    // Devolve TUDO o que foi pago (principal e adicional) e descarta cobranças não pagas.
    // Se o estorno falhar no Mercado Pago, o cancelamento inteiro é desfeito (exceção).
    @Transactional
    public String estornarTudo(Chamado chamado) {
        BigDecimal estornado = BigDecimal.ZERO;
        for (Pagamento p : pagamentoRepository.findByChamadoId(chamado.getId())) {
            if (p.getStatus() == StatusPagamento.pago) {
                boolean real = p.getMpPagamentoId() != null && !"simulado".equals(p.getMpStatusDetalhe()) && mp.configurado();
                if (real) {
                    mp.estornar(p.getMpPagamentoId());
                }
                p.setStatus(StatusPagamento.estornado);
                p.setMpStatus(real ? "refunded" : p.getMpStatus());
                pagamentoRepository.save(p);
                estornado = estornado.add(p.getValor());
            } else if (p.getStatus() != StatusPagamento.estornado) {
                if (p.getMeio() == MeioPagamento.pix && p.getMpPagamentoId() != null && mp.configurado()) {
                    try {
                        mp.cancelar(p.getMpPagamentoId());
                    } catch (MercadoPagoException e) {
                        logger.warn("Não foi possível cancelar o Pix {}: {}", p.getMpPagamentoId(), e.getMessage());
                    }
                }
                pagamentoRepository.delete(p);
                chamado.getPagamentos().remove(p);
            }
        }
        return estornado.signum() > 0 ? "Valor de R$ " + formatar(estornado) + " estornado ao cliente." : null;
    }

    // =====================================================================
    // Depois da aprovação
    // =====================================================================

    // Registra no histórico e, se for o adicional, retoma o atendimento
    private void aoConfirmar(Pagamento p, String como) {
        Chamado chamado = p.getChamado();
        if (p.getTipo() == TipoPagamento.adicional) {
            if (chamado.getStatus() != null && chamado.getStatus().getId() == 6L) {
                chamado.setStatus(statusRepository.getReferenceById(2L));
                chamadoRepository.save(chamado);
            }
            registrar(chamado.getId(), "Adicional de R$ " + formatar(p.getValor()) + " pago pelo cliente (" + como
                    + "). Atendimento retomado.");
        } else {
            registrar(chamado.getId(), "Pagamento de R$ " + formatar(p.getValor()) + " aprovado (" + como
                    + "). Chamado na fila de atendimento.");
        }
    }

    private String descreverForma(Pagamento p) {
        if (p.getForma() == FormaPagamento.pix) return "Pix";
        if (p.getForma() == FormaPagamento.credito) {
            return "crédito" + (p.getCartaoFinal() != null ? " final " + p.getCartaoFinal() : "")
                    + (p.getParcelas() != null && p.getParcelas() > 1 ? " em " + p.getParcelas() + "x" : "");
        }
        if (p.getForma() == FormaPagamento.debito) {
            return "débito" + (p.getCartaoFinal() != null ? " final " + p.getCartaoFinal() : "");
        }
        return p.getBandeira() != null ? p.getBandeira() : "Mercado Pago";
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private Map<String, Object> pagador(Pagamento p) {
        Usuario u = p.getChamado().getUsuario();
        Map<String, Object> pagador = new LinkedHashMap<>();
        String email = (emailPagadorTeste != null && !emailPagadorTeste.isBlank()) ? emailPagadorTeste.trim() : u.getEmail();
        pagador.put("email", email);
        if (u.getNome() != null) {
            String[] partes = u.getNome().trim().split("\\s+", 2);
            pagador.put("first_name", partes[0]);
            if (partes.length > 1) pagador.put("last_name", partes[1]);
        }
        return pagador;
    }

    private String descricao(Pagamento p) {
        return String.format("GMH Support - Chamado #%03d%s", p.getChamado().getId(),
                p.getTipo() == TipoPagamento.adicional ? " (adicional)" : "");
    }

    // Endereço do webhook. Se API_URL_PUBLICA estiver errada, não manda (fica valendo o webhook do painel)
    private String urlNotificacao() {
        String base = urlPublica == null ? "" : urlPublica.trim().replaceAll("/+$", "");
        try {
            java.net.URI uri = java.net.URI.create(base);
            boolean valida = "https".equals(uri.getScheme()) && uri.getHost() != null && uri.getHost().contains(".")
                    && !uri.getHost().contains("localhost");
            if (valida) return base + "/pagamento/webhook";
        } catch (IllegalArgumentException e) {
            // cai no aviso abaixo
        }
        logger.warn("API_URL_PUBLICA inválida ('{}'): cobrança criada sem notification_url", urlPublica);
        return null;
    }

    // Cria a cobrança com notification_url. Se o Mercado Pago recusar o endereço (erro 4020),
    // tenta de novo sem ele: o aviso continua chegando pelo webhook configurado no painel.
    private Map<String, Object> comAviso(Map<String, Object> corpo,
                                         java.util.function.Function<Map<String, Object>, Map<String, Object>> criar) {
        String url = urlNotificacao();
        if (url == null) {
            corpo.remove("notification_url");
            return criar.apply(corpo);
        }
        corpo.put("notification_url", url);
        try {
            return criar.apply(corpo);
        } catch (MercadoPagoException e) {
            if (e.getMessage() == null || !e.getMessage().contains("url")) throw e;
            logger.warn("Mercado Pago recusou notification_url '{}': criando sem ela (use o webhook do painel)", url);
            corpo.remove("notification_url");
            return criar.apply(corpo);
        }
    }

    public static String referencia(Pagamento p) {
        return "gmh-pag-" + p.getId();
    }

    private static Optional<Long> idDaReferencia(String ref) {
        if (ref == null) return Optional.empty();
        Matcher m = REFERENCIA.matcher(ref.trim());
        return m.matches() ? Optional.of(Long.parseLong(m.group(1))) : Optional.empty();
    }

    private boolean chamadoEncerrado(Pagamento p) {
        Long status = p.getChamado().getStatus() != null ? p.getChamado().getStatus().getId() : null;
        return status != null && status == 5L;
    }

    private static BigDecimal valor(Object o) {
        if (o == null) return null;
        try {
            return new BigDecimal(o.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer inteiro(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private void registrar(Long chamadoId, String texto) {
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, null, texto));
    }

    private void validarPendente(Pagamento pagamento) {
        if (pagamento.getStatus() == StatusPagamento.pago) {
            throw new IllegalStateException("Este chamado já foi pago.");
        }
        if (pagamento.getStatus() == StatusPagamento.estornado) {
            throw new IllegalStateException("Este pagamento foi estornado.");
        }
        Long status = pagamento.getChamado().getStatus() != null ? pagamento.getChamado().getStatus().getId() : null;
        if (status != null && (status == 4L || status == 5L)) {
            throw new IllegalStateException("O chamado já foi finalizado ou cancelado.");
        }
    }

    private Chamado buscarChamado(Long chamadoId) {
        return chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));
    }

    private Pagamento buscarDoDono(Long chamadoId, String email, TipoPagamento tipo) {
        Chamado chamado = buscarChamado(chamadoId);
        if (chamado.getUsuario() == null || !chamado.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Somente o dono do chamado pode pagar.");
        }
        return pagamentoRepository.findByChamadoIdAndTipo(chamadoId, tipo)
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui pagamento " + tipo + "."));
    }

    private void validarAcesso(Chamado chamado, String email) {
        boolean ehDono = chamado.getUsuario() != null && chamado.getUsuario().getEmail().equalsIgnoreCase(email);
        boolean ehTecnico = chamado.getTecnico() != null && chamado.getTecnico().getEmail().equalsIgnoreCase(email);
        if (!ehDono && !ehTecnico) {
            throw new AccessDeniedException("Você não tem acesso ao pagamento deste chamado.");
        }
    }

    public static String formatar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    // Só no modo simulado sem Mercado Pago (rodando local): um "copia e cola" de mentira
    private String codigoPixFicticio(BigDecimal valor) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 25).toUpperCase();
        return "00020126580014BR.GOV.BCB.PIX0136gmhsupport-" + id
                + "5204000053039865406" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + "5802BR5911GMH SUPPORT6009SAO PAULO62070503***6304ABCD";
    }
}
