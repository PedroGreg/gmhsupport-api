package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Tecnico;
import java.time.LocalDateTime;

public record TecnicoResponseDTO(
        Long id,
        String nome,
        String email,
        String especialidade,
        Boolean disponivel,
        LocalDateTime dataCadastro,
        String telefone,
        String chavePix,
        String statusCadastro,   // "pendente", "aprovado" ou "recusado"
        String motivoRecusa
) {
    public TecnicoResponseDTO(Tecnico tecnico){
        this(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                tecnico.getEspecialidade(),
                tecnico.getDisponivel(),
                tecnico.getDataCadastro(),
                tecnico.getTelefone(),
                tecnico.getChavePix(),
                tecnico.getStatusCadastro() != null ? tecnico.getStatusCadastro().name() : null,
                tecnico.getMotivoRecusa()
        );
    }

    // Versão para outras pessoas (cliente vendo o técnico do chamado): sem telefone e sem chave Pix
    public TecnicoResponseDTO publico() {
        return new TecnicoResponseDTO(id, nome, email, especialidade, disponivel, dataCadastro,
                null, null, statusCadastro, null);
    }
}
