package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.request.PagamentoRequestDTO;
import br.com.gmhsupport.dto.response.PagamentoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

// Pagamento feito na ABERTURA do chamado (como no iFood). É SIMULADO, sem banco de verdade:
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

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ChamadoRepository chamadoRepository,
                            CartaoService cartaoService,
                            HistoricoChamadoService historicoChamadoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.chamadoRepository = chamadoRepository;
        this.cartaoService = cartaoService;
        this.historicoChamadoService = historicoChamadoService;
    }

    // Valor = preço base da categoria + acréscimo da prioridade
    public static BigDecimal calcularValor(CategoriaProblema categoria, Prioridade prioridade) {
        BigDecimal base = categoria.getValorBase() != null ? categoria.getValorBase() : BigDecimal.ZERO;
        Prioridade p = prioridade != null ? prioridade : Prioridade.media;
        return base.multiply(BigDecimal.ONE.add(p.getAcrescimo())).setScale(2, RoundingMode.HALF_UP);
    }

    // Chamado pelo ChamadoService.create, na mesma transação da criação do chamado
    @Transactional
    public Pagamento criarNaAbertura(Chamado chamado, FormaPagamento forma, Long cartaoId, Integer parcelas) {
        Pagamento pagamento = new Pagamento(chamado, calcularValor(chamado.getCategoria(), chamado.getPrioridade()));
        aplicarForma(pagamento, chamado.getUsuario(), forma, cartaoId, parcelas);
        logger.info("Pagamento do chamado {}: {} via {}", chamado.getId(), pagamento.getStatus(), forma);
        return pagamentoRepository.save(pagamento);
    }

    // Cancelamento: devolve o valor pago (ou descarta o Pix que não foi pago)
    @Transactional
    public String estornarSeNecessario(Chamado chamado) {
        return pagamentoRepository.findByChamadoId(chamado.getId()).map(p -> {
            if (p.getStatus() == StatusPagamento.pago) {
                p.setStatus(StatusPagamento.estornado);
                pagamentoRepository.save(p);
                return "Pagamento de R$ " + formatar(p.getValor()) + " estornado.";
            }
            if (p.getStatus() != StatusPagamento.estornado) {
                // Pix gerado e não pago: só descarta
                pagamentoRepository.delete(p);
                chamado.setPagamento(null);
            }
            return null;
        }).orElse(null);
    }

    // GET: o dono do chamado ou o técnico responsável podem ver
    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPorChamado(Long chamadoId, String email) {
        Chamado chamado = buscarChamado(chamadoId);
        boolean ehDono = chamado.getUsuario() != null && chamado.getUsuario().getEmail().equalsIgnoreCase(email);
        boolean ehTecnico = chamado.getTecnico() != null && chamado.getTecnico().getEmail().equalsIgnoreCase(email);
        if (!ehDono && !ehTecnico) {
            throw new AccessDeniedException("Você não tem acesso ao pagamento deste chamado.");
        }
        Pagamento pagamento = pagamentoRepository.findByChamadoId(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui pagamento."));
        return new PagamentoResponseDTO(pagamento);
    }

    // POST pagar: troca a forma de um pagamento que ainda não foi pago (ex.: Pix -> cartão)
    @Transactional
    public PagamentoResponseDTO pagar(Long chamadoId, String email, PagamentoRequestDTO dto) {
        Pagamento pagamento = buscarDoDono(chamadoId, email);
        validarPendente(pagamento);
        aplicarForma(pagamento, pagamento.getChamado().getUsuario(), dto.forma(), dto.cartaoId(), dto.parcelas());
        Pagamento salvo = pagamentoRepository.save(pagamento);
        if (salvo.getStatus() == StatusPagamento.pago) {
            registrar(chamadoId, "Pagamento aprovado: R$ " + formatar(salvo.getValor())
                    + " no " + (salvo.getForma() == FormaPagamento.credito ? "crédito" : "débito")
                    + " final " + salvo.getCartaoFinal() + ". Chamado na fila de atendimento.");
        }
        return new PagamentoResponseDTO(salvo);
    }

    // POST confirmar-pix: simula o banco avisando que o Pix caiu
    @Transactional
    public PagamentoResponseDTO confirmarPix(Long chamadoId, String email) {
        Pagamento pagamento = buscarDoDono(chamadoId, email);
        validarPendente(pagamento);
        if (pagamento.getStatus() != StatusPagamento.aguardando_pix) {
            throw new IllegalStateException("Gere o Pix antes de confirmar o pagamento.");
        }
        pagamento.setStatus(StatusPagamento.pago);
        pagamento.setDataPagamento(LocalDateTime.now());
        logger.info("Pix confirmado para o chamado {}", chamadoId);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        registrar(chamadoId, "Pix de R$ " + formatar(salvo.getValor()) + " confirmado. Chamado na fila de atendimento.");
        return new PagamentoResponseDTO(salvo);
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
        if (status != null && status == 5L) {
            throw new IllegalStateException("O chamado foi cancelado.");
        }
    }

    private Chamado buscarChamado(Long chamadoId) {
        return chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));
    }

    private Pagamento buscarDoDono(Long chamadoId, String email) {
        Chamado chamado = buscarChamado(chamadoId);
        if (chamado.getUsuario() == null || !chamado.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Somente o dono do chamado pode pagar.");
        }
        return pagamentoRepository.findByChamadoId(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui pagamento."));
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
