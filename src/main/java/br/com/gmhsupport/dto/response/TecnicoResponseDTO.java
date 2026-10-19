package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Tecnico;
import java.time.LocalDateTime;

public record TecnicoResponseDTO(
        Long id,
        String nome,
        String email,
        String especialidade,
        Boolean disponivel,
        LocalDateTime dataCadastro
) {
    public TecnicoResponseDTO(Tecnico tecnico){
        this(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                tecnico.getEspecialidade(),
                tecnico.getDisponivel(),
                tecnico.getDataCadastro()
        );
    }
}
