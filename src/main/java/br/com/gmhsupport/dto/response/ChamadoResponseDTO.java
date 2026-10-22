package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.Prioridade;

import java.time.LocalDateTime;

public record ChamadoResponseDTO(
        Long id,
        Long usuarioId,
        String usuarioNome,
        String usuarioTelefone,
        String usuarioEndereco,
        Long tecnicoId,
        String tecnicoNome,
        Long categoriaId,
        String categoriaNome,
        Long statusId,
        String statusDescricao,
        String titulo,
        String descricao,
        Prioridade prioridade,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtendimento,
        LocalDateTime dataFechamento
) {
    public ChamadoResponseDTO(Chamado chamado) {
        this(
                // 1. ID do Chamado
                chamado.getId(),

                // 2. Mapeamento Usuário (sempre presente)
                chamado.getUsuario() != null ? chamado.getUsuario().getId() : null,
                chamado.getUsuario() != null ? chamado.getUsuario().getNome() : null,
                chamado.getUsuario() != null ? chamado.getUsuario().getTelefone() : null,
                chamado.getUsuario() != null ? chamado.getUsuario().getEndereco() : null,

                // 3. Mapeamento Técnico (pode ser null)
                chamado.getTecnico() != null ? chamado.getTecnico().getId() : null,
                chamado.getTecnico() != null ? chamado.getTecnico().getNome() : null,

                // 4. Mapeamento Categoria
                chamado.getCategoria() != null ? chamado.getCategoria().getId() : null,
                chamado.getCategoria() != null ? chamado.getCategoria().getNome() : null,

                // 5. Mapeamento Status (ID e depois Descrição)
                chamado.getStatus() != null ? chamado.getStatus().getId() : null,
                chamado.getStatus() != null ? chamado.getStatus().getDescricao() : null,

                // 6. Demais campos do Chamado
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getPrioridade(),
                chamado.getDataAbertura(),
                chamado.getDataAtendimento(),
                chamado.getDataFechamento()
        );
    }
}