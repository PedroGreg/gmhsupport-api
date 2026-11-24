package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.SubcategoriaProblema;

import java.math.BigDecimal;

public record SubcategoriaResponseDTO(
        Long id,
        Long categoriaId,
        String nome,
        String descricao,
        BigDecimal valorBase
) {
    public SubcategoriaResponseDTO(SubcategoriaProblema s) {
        this(s.getId(), s.getCategoria() != null ? s.getCategoria().getId() : null,
                s.getNome(), s.getDescricao(), s.getValorBase());
    }
}
