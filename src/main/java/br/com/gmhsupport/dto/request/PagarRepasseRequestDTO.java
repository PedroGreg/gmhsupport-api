package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.Size;

// Opcional: identificação do Pix feito (ex.: "E2E 1234..." ou "Pix 24/11 16h")
public record PagarRepasseRequestDTO(
        @Size(max = 255) String observacao
) {
}
