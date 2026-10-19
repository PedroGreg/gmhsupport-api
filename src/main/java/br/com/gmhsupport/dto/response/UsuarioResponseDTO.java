package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco,
        LocalDateTime dataCadastro
) {
        public UsuarioResponseDTO(Usuario usuario){
                this(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getTelefone(),
                        usuario.getEndereco(),
                        usuario.getDataCadastro()
                );
        }
}
