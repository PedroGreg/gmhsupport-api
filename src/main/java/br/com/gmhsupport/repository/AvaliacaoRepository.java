package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Page<Avaliacao> findByNota(Integer nota, Pageable pageable);

    boolean existsByChamadoId(Long chamadoId);

    Optional<Avaliacao> findByChamadoId(Long chamadoId);
}
