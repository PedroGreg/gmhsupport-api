package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChamadoMensagemRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        String descricao
){
}
