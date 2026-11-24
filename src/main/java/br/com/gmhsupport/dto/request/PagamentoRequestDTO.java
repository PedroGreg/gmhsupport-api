package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.MeioPagamento;
import jakarta.validation.constraints.NotNull;

// Trocar a forma de um pagamento ainda pendente, ou gerar um Pix novo quando o anterior expirou.
// forma: "pix" ou "cartao" (Checkout Pro do Mercado Pago)
public record PagamentoRequestDTO(
        @NotNull(message = "A forma de pagamento é obrigatória (pix ou cartao)")
        MeioPagamento forma
) {
}
