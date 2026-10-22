package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.Prioridade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    Page<Chamado> findByPrioridade(Prioridade prioridade, Pageable pageable);

    Page<Chamado> findByUsuarioEmailIgnoreCase(String email, Pageable pageable);

    Page<Chamado> findByTecnicoIsNull(Pageable pageable);

    Page<Chamado> findByTecnicoIsNotNull(Pageable pageable);

    Page<Chamado> findByTecnicoEmailIgnoreCase(String email, Pageable pageable);

    // Busca todos os chamados finalizados do técnico para cálculo de métricas
    @Query("SELECT c FROM Chamado c WHERE c.tecnico.id = :tecnicoId AND c.status.id = 4L")
    List<Chamado> findFinalizadosByTecnicoId(@Param("tecnicoId") Long tecnicoId);

    // Consulta otimizada para buscar as notas vinculadas aos chamados do técnico
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.chamado.tecnico.id = :tecnicoId")
    Double findNotaMediaByTecnicoId(@Param("tecnicoId") Long tecnicoId);
}