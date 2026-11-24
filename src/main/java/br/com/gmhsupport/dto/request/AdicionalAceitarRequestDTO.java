package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.FormaPagamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Cliente aceita o adicional e já escolhe como pagar
public record AdicionalAceitarRequestDTO(
        @NotNull(message = "Escolha a forma de pagamento (pix, credito ou debito)")
        FormaPagamento formaPagamento,
        Long cartaoId,
        @Min(1) @Max(6)
        Integer parcelas
) {
}
