package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.CategoriaProblema;

import java.math.BigDecimal;

public record CategoriaProblemaResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorBase
) {
    public CategoriaProblemaResponseDTO(CategoriaProblema categoria){
        this(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao() != null ? categoria.getDescricao() : null,
                categoria.getValorBase()
        );
    }
}