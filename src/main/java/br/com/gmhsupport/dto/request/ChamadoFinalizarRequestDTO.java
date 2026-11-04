package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ChamadoFinalizarRequestDTO(
        @NotBlank(message = "A descrição é obrigatoria")
        String descricao,

        // Valor do serviço. Vazio ou 0 = sem cobrança. Maior que 0 = gera um pagamento pendente para o cliente.
        @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
        @DecimalMax(value = "99999.99", message = "Valor muito alto")
        @Digits(integer = 5, fraction = 2, message = "Use no máximo 2 casas decimais")
        BigDecimal valor
) {
}
