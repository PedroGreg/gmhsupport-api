package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequestDTO(
        Long tecnicoId,

        @NotNull(message = "A categoria é obrigatoria")
        Long categoriaId,

        @NotBlank(message = "O titulo é obrigatorio")
        @Size(max = 155)
        String titulo,

        @NotBlank(message = "A descrição é obrigatoria")
        String descricao,

        Prioridade prioridade
) {
}
