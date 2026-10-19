package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.HistoricoChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {
    Page<HistoricoChamado> findByChamadoIdOrderByDataRegistroAsc(Long chamadoId, Pageable pageable);
}
