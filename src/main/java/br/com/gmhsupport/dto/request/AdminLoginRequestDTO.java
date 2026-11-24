package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequestDTO(
        @NotBlank(message = "Informe o e-mail") String email,
        @NotBlank(message = "Informe a senha") String senha
) {
}
