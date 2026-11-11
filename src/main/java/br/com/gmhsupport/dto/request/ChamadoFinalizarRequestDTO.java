package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChamadoFinalizarRequestDTO(
        @NotBlank(message = "A descrição é obrigatoria")
        String descricao
) {
}
