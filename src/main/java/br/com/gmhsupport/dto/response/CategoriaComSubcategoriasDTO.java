package br.com.gmhsupport.dto.response;

import java.util.List;

// Árvore usada pelo app no Novo Chamado: categoria + os problemas (subcategorias) com preço
public record CategoriaComSubcategoriasDTO(
        Long id,
        String nome,
        String descricao,
        Boolean valorFixo,
        List<SubcategoriaResponseDTO> subcategorias
) {
}
