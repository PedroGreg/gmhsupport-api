package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.MeioPagamento;
import br.com.gmhsupport.model.Prioridade;
import jakarta.validation.constraints.*;

public record ChamadoCreateRequestDTO(

        // O problema escolhido. A categoria vem dela.
        @NotNull(message = "Escolha o problema (subcategoria)")
        Long subcategoriaId,

        @NotBlank(message = "O titulo é obrigatorio")
        @Size(max = 150)
        String titulo,

        @NotBlank(message = "A descrição é obrigatoria")
        @Size(max = 4000)
        String descricao,

        Prioridade prioridade,

        // Local do atendimento (GPS do celular ou endereço digitado e convertido pelo app)
        @NotNull(message = "Informe a localização do atendimento")
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        Double latitude,

        @NotNull(message = "Informe a localização do atendimento")
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        Double longitude,

        @NotBlank(message = "Informe o endereço do atendimento")
        @Size(max = 255)
        String enderecoAtendimento,

        @Size(max = 120)
        String complemento,

        // Pagamento na abertura (como no iFood), pelo Mercado Pago:
        //  "pix"    -> a API devolve o código Pix (copia e cola)
        //  "cartao" -> a API devolve o link do Checkout Pro (crédito/débito, parcelas escolhidas lá)
        @NotNull(message = "Escolha a forma de pagamento (pix ou cartao)")
        MeioPagamento formaPagamento
) {
}
