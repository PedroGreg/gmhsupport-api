package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 2º passo do finalizar: o técnico digita o código que o cliente vê no app
public record ChamadoFinalizarRequestDTO(
        @NotBlank(message = "Informe o código de confirmação")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 números")
        String codigo
) {
}
