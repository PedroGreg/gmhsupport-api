package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.request.PagamentoRequestDTO;
import br.com.gmhsupport.dto.response.PagamentoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.PagamentoRepository;
import br.com.gmhsupport.repository.StatusChamadoRepository;
import br.com.gmhsupport.dto.response.OrcamentoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Pagamentos de um chamado: o PRINCIPAL (na abertura, como no iFood) e o ADICIONAL (se o técnico pedir).
// Ainda SIMULADO (o Mercado Pago entra na Etapa 3):
//  - Pix: gera um código "copia e cola"; o chamado só entra na fila depois do "Já paguei"
//  - Crédito/Débito: usa um cartão salvo e aprova na hora
//  - Chamado cancelado depois de pago: o valor é estornado
@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final ChamadoRepository chamadoRepository;
    private final CartaoService cartaoService;
    private final HistoricoChamadoService historicoChamadoService;
    private final StatusChamadoRepository statusRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ChamadoRepository chamadoRepository,
                            CartaoService cartaoService,
                            HistoricoChamadoService historicoChamadoService,
                            StatusChamadoRepository statusRepository) {
        this.statusRepository = statusRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.chamadoRepository = chamadoRepository;
        this.cartaoService = cartaoService;
        this.historicoChamadoService = historicoChamadoService;
    }

    // Pagamento principal, criado junto com o chamado (mesma transação).
    // O valor vem do orçamento (PrecoService): serviço + deslocamento.
    @Transactional
    public Pagamento criarNaAbertura(Chamado chamado, OrcamentoResponseDTO orcamento,
                                     FormaPagamento forma, Long cartaoId, Integer parcelas) {
        Pagamento pagamento = new Pagamento(chamado, orcamento.total());
        pagamento.setTipo(TipoPagamento.principal);
        pagamento.setValorServico(orcamento.valorServico());
        pagamento.setValorDeslocamento(orcamento.valorDeslocamento());
        aplicarForma(pagamento, chamado.getUsuario(), forma, cartaoId, parcelas);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        chamado.getPagamentos().add(salvo);
        logger.info("Pagamento principal do chamado {}: {} via {}", chamado.getId(), salvo.getStatus(), forma);
        return salvo;
    }

    // Pagamento do valor adicional, criado quando o cliente aceita o pedido do técnico
    @Transactional
    public Pagamento criarAdicional(Chamado chamado, BigDecimal valor,
                                    FormaPagamento forma, Long cartaoId, Integer parcelas) {
        if (chamado.getPagamentoAdicional() != null) {
            throw new IllegalStateException("O adicional deste chamado já tem um pagamento.");
        }
        Pagamento pagamento = new Pagamento(chamado, valor.setScale(2, RoundingMode.HALF_UP));
        pagamento.setTipo(TipoPagamento.adicional);
        aplicarForma(pagamento, chamado.getUsuario(), forma, cartaoId, parcelas);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        chamado.getPagamentos().add(salvo);
        if (salvo.getStatus() == StatusPagamento.pago) aoPagarAdicional(chamado, salvo);
        return salvo;
    }

    // Cancelamento: devolve TUDO o que foi pago (principal e adicional) e descarta Pix não pagos.
    // Devolve o texto para o histórico (ou null se não havia nada pago).
    @Transactional
    public String estornarTudo(Chamado chamado) {
        BigDecimal estornado = BigDecimal.ZERO;
        for (Pagamento p : pagamentoRepository.findByChamadoId(chamado.getId())) {
            if (p.getStatus() == StatusPagamento.pago) {
                p.setStatus(StatusPagamento.estornado);
                pagamentoRepository.save(p);
                estornado = estornado.add(p.getValor());
            } else if (p.getStatus() != StatusPagamento.estornado) {
                pagamentoRepository.delete(p);
                chamado.getPagamentos().remove(p);
            }
        }
        return estornado.signum() > 0 ? "Valor de R$ " + formatar(estornado) + " estornado ao cliente." : null;
    }

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

    // POST pagar: troca a forma de um pagamento que ainda não foi pago (ex.: Pix -> cartão)
    @Transactional
    public PagamentoResponseDTO pagar(Long chamadoId, String email, PagamentoRequestDTO dto, TipoPagamento tipo) {
        Pagamento pagamento = buscarDoDono(chamadoId, email, tipo);
        validarPendente(pagamento);
        aplicarForma(pagamento, pagamento.getChamado().getUsuario(), dto.forma(), dto.cartaoId(), dto.parcelas());
        Pagamento salvo = pagamentoRepository.save(pagamento);
        if (salvo.getStatus() == StatusPagamento.pago) {
            aoConfirmar(salvo, (salvo.getForma() == FormaPagamento.credito ? "crédito" : "débito")
                    + " final " + salvo.getCartaoFinal());
        }
        return new PagamentoResponseDTO(salvo);
    }

    // POST confirmar-pix: simula o banco avisando que o Pix caiu
    @Transactional
    public PagamentoResponseDTO confirmarPix(Long chamadoId, String email, TipoPagamento tipo) {
        Pagamento pagamento = buscarDoDono(chamadoId, email, tipo);
        validarPendente(pagamento);
        if (pagamento.getStatus() != StatusPagamento.aguardando_pix) {
            throw new IllegalStateException("Gere o Pix antes de confirmar o pagamento.");
        }
        pagamento.setStatus(StatusPagamento.pago);
        pagamento.setDataPagamento(LocalDateTime.now());
        logger.info("Pix ({}) confirmado para o chamado {}", tipo, chamadoId);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        aoConfirmar(salvo, "Pix");
        return new PagamentoResponseDTO(salvo);
    }

    // Depois de um pagamento confirmado: registra no histórico e, se for o adicional, retoma o atendimento
    private void aoConfirmar(Pagamento p, String como) {
        Chamado chamado = p.getChamado();
        if (p.getTipo() == TipoPagamento.adicional) {
            aoPagarAdicional(chamado, p);
        } else {
            registrar(chamado.getId(), "Pagamento de R$ " + formatar(p.getValor()) + " aprovado (" + como
                    + "). Chamado na fila de atendimento.");
        }
    }

    private void aoPagarAdicional(Chamado chamado, Pagamento p) {
        if (chamado.getStatus() != null && chamado.getStatus().getId() == 6L) {
            chamado.setStatus(statusRepository.getReferenceById(2L));
            chamadoRepository.save(chamado);
        }
        registrar(chamado.getId(), "Adicional de R$ " + formatar(p.getValor()) + " pago pelo cliente. Atendimento retomado.");
    }

    // ----- regras -----

    private void registrar(Long chamadoId, String texto) {
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, null, texto));
    }

    private void aplicarForma(Pagamento pagamento, Usuario usuario, FormaPagamento forma, Long cartaoId, Integer parcelas) {
        pagamento.setForma(forma);

        if (forma == FormaPagamento.pix) {
            pagamento.setParcelas(1);
            pagamento.setCartaoFinal(null);
            pagamento.setBandeira(null);
            pagamento.setCodigoPix(gerarCodigoPix(pagamento.getValor()));
            pagamento.setStatus(StatusPagamento.aguardando_pix);
            return;
        }

        if (cartaoId == null) {
            throw new IllegalStateException("Escolha um cartão para pagar no " + (forma == FormaPagamento.credito ? "crédito." : "débito."));
        }
        Cartao cartao = cartaoService.buscarParaPagamento(cartaoId, usuario.getId());
        if (!cartao.getTipo().name().equals(forma.name())) {
            throw new IllegalStateException("O cartão final " + cartao.getUltimosDigitos() + " é de " + cartao.getTipo() + ".");
        }

        pagamento.setParcelas(forma == FormaPagamento.credito && parcelas != null ? parcelas : 1);
        pagamento.setCartaoFinal(cartao.getUltimosDigitos());
        pagamento.setBandeira(cartao.getBandeira());
        pagamento.setCodigoPix(null);
        pagamento.setStatus(StatusPagamento.pago); // simulação: aprovado na hora
        pagamento.setDataPagamento(LocalDateTime.now());
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

    // Código no formato de um Pix "copia e cola" (fictício)
    private String gerarCodigoPix(BigDecimal valor) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 25).toUpperCase();
        return "00020126580014BR.GOV.BCB.PIX0136gmhsupport-" + id
                + "5204000053039865406" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString()
                + "5802BR5911GMH SUPPORT6009SAO PAULO62070503***6304ABCD";
    }
}
