package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TecnicoRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
    String nome,

    @NotBlank(message = "O email é obrigatório")
    @Email
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 100, message = "A senha deve ter no minimo 6 caracteres e no máximo 100")
    String senha,

    String especialidade
) {
}
