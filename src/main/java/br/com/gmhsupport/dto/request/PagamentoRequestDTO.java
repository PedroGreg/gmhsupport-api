package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.FormaPagamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Pagamento SIMULADO: nenhum dado de cartão é enviado para a API, só os 4 últimos dígitos
// para mostrar no comprovante. Em produção, os dados do cartão iriam direto para um
// gateway (Mercado Pago, Stripe, PagSeguro...) e a API receberia só a confirmação.
public record PagamentoRequestDTO(
        @NotNull(message = "A forma de pagamento é obrigatória (pix, credito ou debito)")
        FormaPagamento forma,

        @Min(value = 1, message = "Mínimo de 1 parcela")
        @Max(value = 12, message = "Máximo de 12 parcelas")
        Integer parcelas,

        @Pattern(regexp = "\\d{4}", message = "Informe apenas os 4 últimos dígitos do cartão")
        String cartaoFinal
) {
}
