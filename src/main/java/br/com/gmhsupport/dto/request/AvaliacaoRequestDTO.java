package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoRequestDTO(
        @NotNull(message = "O chamado é obrigatório")
        Long chamadoId,

        @NotNull(message = "A nota é obrigatória")
        @Min(value = 0, message = "A nota minima é 0")
        @Max(value = 5, message = "A nota máxima é 5")
        Integer nota,

        String comentario
) {
}
