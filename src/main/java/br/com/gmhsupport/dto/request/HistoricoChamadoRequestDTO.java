package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HistoricoChamadoRequestDTO(
        @NotNull(message = "O id do chamado é obrigatório")
        Long chamadoId,
        Long tecnicoId,
        @NotBlank(message = "A descrição é obrigatória")
        String descricao
) {
}
