package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.Prioridade;
import br.com.gmhsupport.model.Tecnico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    Page<Chamado> findByPrioridade(Prioridade prioridade, Pageable pageable);

    Page<Chamado> findByUsuarioEmailIgnoreCase(String email, Pageable pageable);

    Page<Chamado> findByTecnicoIsNull(Pageable pageable);

    Page<Chamado> findByTecnicoIsNotNull(Pageable pageable);

    Page<Chamado> findByTecnicoEmailIgnoreCase(String email, Pageable pageable);
}