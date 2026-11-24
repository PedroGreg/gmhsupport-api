package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.MeioPagamento;
import jakarta.validation.constraints.NotNull;

// Cliente aceita o adicional e já escolhe como pagar: "pix" ou "cartao" (Checkout Pro do Mercado Pago)
public record AdicionalAceitarRequestDTO(
        @NotNull(message = "Escolha a forma de pagamento (pix ou cartao)")
        MeioPagamento formaPagamento
) {
}
