package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.HistoricoChamado;

import java.time.LocalDateTime;

public record HistoricoChamadoResponseDTO(
        Long id,
        Long chamadoId,
        String chamadoTitulo,
        Long tecnicoId,
        String tecnicoNome,
        String descricao,
        LocalDateTime dataRegistro
) {
    public HistoricoChamadoResponseDTO(HistoricoChamado historico){
        this(
                historico.getId(),

                historico.getChamado().getId() != null ? historico.getChamado().getId() : null,
                historico.getChamado().getTitulo() != null ? historico.getChamado().getTitulo() : null,

                historico.getTecnico() != null ? historico.getTecnico().getId() : null,
                historico.getTecnico() != null ? historico.getTecnico().getNome() : null,

                historico.getDescricao(),
                historico.getDataRegistro()
        );
    }
}
