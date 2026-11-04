package br.com.gmhsupport.services;

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
import java.time.LocalDateTime;
import java.util.UUID;

// Pagamento SIMULADO (não há banco/gateway de verdade):
//  - Pix: gera um código "copia e cola" e o cliente confirma com "Já paguei" (simula o aviso do banco)
//  - Crédito/Débito: aprova na hora, guardando só os 4 últimos dígitos do cartão
@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final ChamadoRepository chamadoRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository, ChamadoRepository chamadoRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    // Chamado pelo ChamadoService quando o técnico finaliza informando um valor
    @Transactional
    public Pagamento criarCobranca(Chamado chamado, BigDecimal valor) {
        if (pagamentoRepository.findByChamadoId(chamado.getId()).isPresent()) {
            throw new IllegalStateException("Este chamado já possui uma cobrança.");
        }
        logger.info("Gerando cobrança de {} para o chamado {}", valor, chamado.getId());
        return pagamentoRepository.save(new Pagamento(chamado, valor));
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
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui cobrança."));
        return new PagamentoResponseDTO(pagamento);
    }

    // POST pagar: só o dono do chamado
    @Transactional
    public PagamentoResponseDTO pagar(Long chamadoId, String email, PagamentoRequestDTO dto) {
        Pagamento pagamento = buscarDoDono(chamadoId, email);

        if (pagamento.getStatus() == StatusPagamento.pago) {
            throw new IllegalStateException("Este chamado já foi pago.");
        }

        pagamento.setForma(dto.forma());

        if (dto.forma() == FormaPagamento.pix) {
            // Gera (ou reaproveita) o código Pix e fica aguardando a confirmação
            if (pagamento.getCodigoPix() == null) {
                pagamento.setCodigoPix(gerarCodigoPix(pagamento));
            }
            pagamento.setParcelas(1);
            pagamento.setCartaoFinal(null);
            pagamento.setStatus(StatusPagamento.aguardando_pix);
            logger.info("Pix gerado para o chamado {}", chamadoId);
        } else {
            if (dto.cartaoFinal() == null) {
                throw new IllegalStateException("Informe os 4 últimos dígitos do cartão.");
            }
            int parcelas = dto.forma() == FormaPagamento.credito && dto.parcelas() != null ? dto.parcelas() : 1;
            pagamento.setParcelas(parcelas);
            pagamento.setCartaoFinal(dto.cartaoFinal());
            pagamento.setStatus(StatusPagamento.pago);
            pagamento.setDataPagamento(LocalDateTime.now());
            logger.info("Pagamento no cartão aprovado para o chamado {}", chamadoId);
        }

        return new PagamentoResponseDTO(pagamentoRepository.save(pagamento));
    }

    // POST confirmar-pix: simula o banco avisando que o Pix caiu
    @Transactional
    public PagamentoResponseDTO confirmarPix(Long chamadoId, String email) {
        Pagamento pagamento = buscarDoDono(chamadoId, email);

        if (pagamento.getStatus() == StatusPagamento.pago) {
            throw new IllegalStateException("Este chamado já foi pago.");
        }
        if (pagamento.getStatus() != StatusPagamento.aguardando_pix) {
            throw new IllegalStateException("Gere o Pix antes de confirmar o pagamento.");
        }

        pagamento.setStatus(StatusPagamento.pago);
        pagamento.setDataPagamento(LocalDateTime.now());
        logger.info("Pix confirmado para o chamado {}", chamadoId);
        return new PagamentoResponseDTO(pagamentoRepository.save(pagamento));
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
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não possui cobrança."));
    }

    // Código no formato de um Pix "copia e cola" (fictício)
    private String gerarCodigoPix(Pagamento pagamento) {
        String valor = pagamento.getValor().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 25).toUpperCase();
        return "00020126580014BR.GOV.BCB.PIX0136gmhsupport-" + id
                + "5204000053039865406" + valor
                + "5802BR5911GMH SUPPORT6009SAO PAULO62070503***6304ABCD";
    }
}
