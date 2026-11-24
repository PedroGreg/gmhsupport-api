package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.Tecnico;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

// Técnico como o administrador vê (dados completos para a análise)
public record TecnicoAdminResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        LocalDate dataNascimento,
        Integer idade,
        String especialidade,
        String chavePix,
        String statusCadastro,
        String motivoRecusa,
        LocalDateTime dataCadastro,
        LocalDateTime dataAnalise,
        LocalDateTime termosAceitosEm,
        Boolean temDocumento,
        String documentoNome,
        String documentoTipo,
        Long documentoTamanho,
        LocalDateTime documentoEnviadoEm
) {
    public TecnicoAdminResponseDTO(Tecnico t, DocumentoInfoDTO d) {
        this(t.getId(), t.getNome(), t.getEmail(), t.getCpf(), t.getTelefone(), t.getDataNascimento(),
                t.getDataNascimento() != null ? Period.between(t.getDataNascimento(), LocalDate.now()).getYears() : null,
                t.getEspecialidade(), t.getChavePix(),
                t.getStatusCadastro() != null ? t.getStatusCadastro().name() : null,
                t.getMotivoRecusa(), t.getDataCadastro(), t.getDataAnalise(), t.getTermosAceitosEm(),
                d != null,
                d != null ? d.nomeArquivo() : null,
                d != null ? d.tipoConteudo() : null,
                d != null ? d.tamanho() : null,
                d != null ? d.dataEnvio() : null);
    }
}
