package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.response.RepassePagoResponseDTO;
import br.com.gmhsupport.dto.response.RepasseResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.PagamentoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

// Repasse ao técnico: quando o chamado é finalizado com o código do cliente, cada pagamento pago
// fica "liberado" com 80% (configurável) para o técnico. O admin faz o Pix e marca como "pago".
@Service
public class RepasseService {

    private static final Logger logger = LoggerFactory.getLogger(RepasseService.class);

    private final PagamentoRepository pagamentoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final HistoricoChamadoService historicoChamadoService;
    private final BigDecimal percentual;

    public RepasseService(PagamentoRepository pagamentoRepository,
                          TecnicoRepository tecnicoRepository,
                          HistoricoChamadoService historicoChamadoService,
                          @Value("${gmh.repasse.percentual:0.80}") BigDecimal percentual) {
        this.pagamentoRepository = pagamentoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.historicoChamadoService = historicoChamadoService;
        this.percentual = percentual;
    }

    public int percentualInteiro() {
        return percentual.multiply(BigDecimal.valueOf(100)).intValue();
    }

    // Chamado pelo ChamadoService.finalizarChamado (mesma transação)
    @Transactional
    public BigDecimal liberar(Chamado chamado) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pagamento p : pagamentoRepository.findByChamadoId(chamado.getId())) {
            if (p.getStatus() == StatusPagamento.pago && p.getRepasseStatus() == StatusRepasse.nao_liberado) {
                p.setValorRepasse(p.getValor().multiply(percentual).setScale(2, RoundingMode.HALF_UP));
                p.setRepasseStatus(StatusRepasse.liberado);
                p.setDataRepasseLiberado(LocalDateTime.now());
                pagamentoRepository.save(p);
                total = total.add(p.getValorRepasse());
            }
        }
        logger.info("Chamado {}: repasse liberado de {}", chamado.getId(), total);
        return total;
    }

    @Transactional(readOnly = true)
    public List<RepasseResponseDTO> listar(StatusRepasse status) {
        return pagamentoRepository.listarRepasses(status).stream().map(RepasseResponseDTO::new).toList();
    }

    // Admin fez UM Pix com tudo o que estava liberado para o técnico
    @Transactional
    public RepassePagoResponseDTO pagarTudoDoTecnico(Long tecnicoId, String observacao, String emailAdmin) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado: " + tecnicoId));
        List<Pagamento> liberados = pagamentoRepository.repassesDoTecnico(tecnicoId, StatusRepasse.liberado);
        if (liberados.isEmpty()) {
            throw new IllegalStateException("Este técnico não tem repasses a pagar.");
        }
        LocalDateTime agora = LocalDateTime.now();
        BigDecimal total = BigDecimal.ZERO;
        String obs = (observacao == null || observacao.isBlank()) ? "Pix enviado por " + emailAdmin : observacao.trim();
        for (Pagamento p : liberados) {
            p.setRepasseStatus(StatusRepasse.pago);
            p.setDataRepassePago(agora);
            p.setObservacaoRepasse(obs.length() > 255 ? obs.substring(0, 255) : obs);
            pagamentoRepository.save(p);
            total = total.add(p.getValorRepasse());
        }
        for (Long chamadoId : liberados.stream().map(p -> p.getChamado().getId()).distinct().toList()) {
            historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, null,
                    "Repasse pago ao técnico pela plataforma."));
        }
        logger.info("Admin {} pagou {} ao técnico {} ({} pagamentos)", emailAdmin, total, tecnicoId, liberados.size());
        return new RepassePagoResponseDTO(tecnico.getId(), tecnico.getNome(), tecnico.getChavePix(), liberados.size(), total);
    }

    public BigDecimal somaDoTecnico(Long tecnicoId, StatusRepasse status) {
        BigDecimal soma = pagamentoRepository.somarRepassePorTecnico(tecnicoId, status);
        return soma != null ? soma : BigDecimal.ZERO;
    }
}
