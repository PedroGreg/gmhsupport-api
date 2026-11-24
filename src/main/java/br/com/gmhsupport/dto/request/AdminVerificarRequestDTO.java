package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminVerificarRequestDTO(
        @NotBlank(message = "Token temporário ausente") String tokenTemporario,
        @NotBlank(message = "Informe o código")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 números")
        String codigo
) {
}
