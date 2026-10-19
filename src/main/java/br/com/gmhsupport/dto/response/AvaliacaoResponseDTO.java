package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Avaliacao;
import br.com.gmhsupport.model.Chamado;

import java.time.LocalDateTime;

public record AvaliacaoResponseDTO(
        Long id,
        Long chamadoId,
        String chamadoTitulo,
        Integer nota,
        String comentario,
        LocalDateTime dataAvaliacao
) {
    public AvaliacaoResponseDTO(Avaliacao avaliacao){
        this(
                avaliacao.getId(),
                avaliacao.getChamado().getId() != null ? avaliacao.getChamado().getId() : null,
                avaliacao.getChamado().getTitulo() != null ? avaliacao.getChamado().getTitulo() : null,
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataAvaliacao()
        );
    }
}
