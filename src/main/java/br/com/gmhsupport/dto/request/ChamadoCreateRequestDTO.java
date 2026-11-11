package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.FormaPagamento;
import br.com.gmhsupport.model.Prioridade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoCreateRequestDTO(

        @NotNull(message = "A categoria é obrigatoria")
        Long categoriaId,

        @NotBlank(message = "O titulo é obrigatorio")
        @Size(max = 155)
        String titulo,

        @NotBlank(message = "A descrição é obrigatoria")
        String descricao,

        Prioridade prioridade,

        // Pagamento na abertura (como no iFood)
        @NotNull(message = "Escolha a forma de pagamento (pix, credito ou debito)")
        FormaPagamento formaPagamento,

        // Cartão salvo em "Meus cartões". Obrigatório para credito/debito
        Long cartaoId,

        // Só no crédito: 1 a 6
        @Min(value = 1, message = "Mínimo de 1 parcela")
        @Max(value = 6, message = "Máximo de 6 parcelas")
        Integer parcelas
) {
}
