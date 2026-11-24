package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 1º passo do finalizar: o técnico descreve a solução e a API gera o código para o cliente
public record SolicitarCodigoRequestDTO(
        @NotBlank(message = "Descreva a solução aplicada")
        @Size(min = 10, max = 4000, message = "A solução deve ter entre 10 e 4000 caracteres")
        String descricao
) {
}
