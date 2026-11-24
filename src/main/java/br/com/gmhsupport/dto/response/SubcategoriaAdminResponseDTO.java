package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.SubcategoriaProblema;

import java.math.BigDecimal;

public record SubcategoriaAdminResponseDTO(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String nome,
        String descricao,
        BigDecimal valorBase,
        Boolean ativo
) {
    public SubcategoriaAdminResponseDTO(SubcategoriaProblema s) {
        this(s.getId(),
                s.getCategoria() != null ? s.getCategoria().getId() : null,
                s.getCategoria() != null ? s.getCategoria().getNome() : null,
                s.getNome(), s.getDescricao(), s.getValorBase(), s.getAtivo());
    }
}
