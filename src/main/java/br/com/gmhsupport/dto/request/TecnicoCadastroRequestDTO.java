package br.com.gmhsupport.dto.request;

import java.time.LocalDate;
import java.util.List;

// Parte "dados" (JSON) do POST /tecnico/cadastro (multipart). A outra parte é o arquivo "antecedentes".
// A validação fica no TecnicoCadastroService, com mensagens em português para o app.
public record TecnicoCadastroRequestDTO(
        String nome,
        String email,
        String senha,
        String cpf,              // com ou sem pontuação
        String telefone,
        LocalDate dataNascimento, // "AAAA-MM-DD"
        List<String> especialidades,
        String chavePix,
        Boolean aceitouTermos
) {
}
