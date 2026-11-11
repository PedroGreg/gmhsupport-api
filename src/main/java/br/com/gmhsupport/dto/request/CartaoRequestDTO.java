package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.TipoCartao;
import jakarta.validation.constraints.*;

// O app valida o número completo (Luhn) e descobre a bandeira; aqui chegam só os dados "de exibição".
public record CartaoRequestDTO(
        @Size(max = 40, message = "O apelido deve ter no máximo 40 caracteres")
        String apelido,

        @NotNull(message = "O tipo é obrigatório (credito ou debito)")
        TipoCartao tipo,

        @NotBlank(message = "A bandeira é obrigatória")
        @Size(max = 20)
        String bandeira,

        @NotNull(message = "Os 4 últimos dígitos são obrigatórios")
        @Pattern(regexp = "\\d{4}", message = "Envie apenas os 4 últimos dígitos do cartão")
        String ultimosDigitos,

        @NotBlank(message = "O nome do titular é obrigatório")
        @Size(max = 100)
        String nomeTitular,

        @NotNull @Min(value = 1, message = "Mês inválido") @Max(value = 12, message = "Mês inválido")
        Integer mesValidade,

        @NotNull @Min(value = 2000, message = "Ano inválido") @Max(value = 2100, message = "Ano inválido")
        Integer anoValidade,

        Boolean principal
) {
}
