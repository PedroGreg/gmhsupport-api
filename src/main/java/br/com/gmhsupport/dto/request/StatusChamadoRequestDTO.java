package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusChamadoRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 50, message = "A descrição deve ter no máximo 50 caracteres")
        String descricao
) {
}
