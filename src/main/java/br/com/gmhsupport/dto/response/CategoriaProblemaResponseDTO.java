package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.CategoriaProblema;

public record CategoriaProblemaResponseDTO(
        Long id,
        String nome,
        String descricao
) {
    public CategoriaProblemaResponseDTO(CategoriaProblema categoria){
        this(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao() != null ? categoria.getDescricao() : null
        );
    }
}