package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.FormaPagamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Usado para trocar a forma de um pagamento ainda pendente (ex.: gerou Pix e quer pagar no cartão).
// Pagamento SIMULADO: o cartão é um dos cartões salvos do usuário (só os 4 últimos dígitos ficam na API).
public record PagamentoRequestDTO(
        @NotNull(message = "A forma de pagamento é obrigatória (pix, credito ou debito)")
        FormaPagamento forma,

        // Obrigatório para credito/debito
        Long cartaoId,

        @Min(value = 1, message = "Mínimo de 1 parcela")
        @Max(value = 6, message = "Máximo de 6 parcelas")
        Integer parcelas
) {
}
