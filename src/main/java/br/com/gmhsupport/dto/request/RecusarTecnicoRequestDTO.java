package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecusarTecnicoRequestDTO(
        @NotBlank(message = "Informe o motivo da recusa")
        @Size(min = 5, max = 500, message = "O motivo deve ter entre 5 e 500 caracteres")
        String motivo
) {
}
