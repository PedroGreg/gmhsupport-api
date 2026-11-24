package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.Pagamento;
import br.com.gmhsupport.model.Tecnico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Um pagamento do cliente e quanto dele vai para o técnico (repasse) e para a plataforma
public record RepasseResponseDTO(
        Long pagamentoId,
        Long chamadoId,
        String chamadoTitulo,
        String tipo,
        Long tecnicoId,
        String tecnicoNome,
        String chavePix,
        BigDecimal valorPago,
        BigDecimal valorRepasse,
        BigDecimal valorPlataforma,
        String repasseStatus,
        LocalDateTime dataLiberado,
        LocalDateTime dataPago,
        String observacao
) {
    public RepasseResponseDTO(Pagamento p) {
        this(p.getId(),
                p.getChamado().getId(),
                p.getChamado().getTitulo(),
                p.getTipo() != null ? p.getTipo().name() : null,
                tecnico(p.getChamado()) != null ? tecnico(p.getChamado()).getId() : null,
                tecnico(p.getChamado()) != null ? tecnico(p.getChamado()).getNome() : null,
                tecnico(p.getChamado()) != null ? tecnico(p.getChamado()).getChavePix() : null,
                p.getValor(),
                p.getValorRepasse(),
                p.getValorRepasse() != null ? p.getValor().subtract(p.getValorRepasse()) : null,
                p.getRepasseStatus() != null ? p.getRepasseStatus().name() : null,
                p.getDataRepasseLiberado(),
                p.getDataRepassePago(),
                p.getObservacaoRepasse());
    }

    private static Tecnico tecnico(Chamado c) {
        return c != null ? c.getTecnico() : null;
    }
}
