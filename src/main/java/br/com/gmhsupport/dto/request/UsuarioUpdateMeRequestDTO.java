package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateMeRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String telefone,

        String endereco
) {
}
