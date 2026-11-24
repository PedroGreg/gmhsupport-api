package br.com.gmhsupport.repository;

import br.com.gmhsupport.dto.response.DocumentoInfoDTO;
import br.com.gmhsupport.model.DocumentoTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentoTecnicoRepository extends JpaRepository<DocumentoTecnico, Long> {
    Optional<DocumentoTecnico> findByTecnicoId(Long tecnicoId);

    @Query("SELECT new br.com.gmhsupport.dto.response.DocumentoInfoDTO(d.tecnico.id, d.nomeArquivo, d.tipoConteudo, d.tamanho, d.dataEnvio) " +
           "FROM DocumentoTecnico d")
    List<DocumentoInfoDTO> listarInfos();

    @Query("SELECT new br.com.gmhsupport.dto.response.DocumentoInfoDTO(d.tecnico.id, d.nomeArquivo, d.tipoConteudo, d.tamanho, d.dataEnvio) " +
           "FROM DocumentoTecnico d WHERE d.tecnico.id = :tecnicoId")
    Optional<DocumentoInfoDTO> infoDoTecnico(@Param("tecnicoId") Long tecnicoId);
}
