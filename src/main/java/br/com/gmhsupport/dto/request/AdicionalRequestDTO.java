package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// Técnico pede um valor a mais (serviço mais complexo ou diferente do solicitado)
public record AdicionalRequestDTO(
        @NotNull(message = "Informe o valor do adicional")
        @DecimalMin(value = "1.00", message = "O adicional mínimo é R$ 1,00")
        @DecimalMax(value = "99999.99", message = "Valor muito alto")
        @Digits(integer = 5, fraction = 2, message = "Use no máximo 2 casas decimais")
        BigDecimal valor,

        @NotBlank(message = "Explique o motivo do adicional")
        @Size(min = 10, max = 1000, message = "O motivo deve ter entre 10 e 1000 caracteres")
        String motivo
) {
}
