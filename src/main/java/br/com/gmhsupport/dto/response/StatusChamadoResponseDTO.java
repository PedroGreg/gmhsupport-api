package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.StatusChamado;

public record StatusChamadoResponseDTO (
        Long id,
        String descricao
){
    public StatusChamadoResponseDTO(StatusChamado status){
        this(
                status != null ? status.getId() : null,
                status != null ? status.getDescricao() : null
        );
    }
}
