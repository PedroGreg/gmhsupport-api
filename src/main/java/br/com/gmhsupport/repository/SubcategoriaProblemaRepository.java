package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.SubcategoriaProblema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoriaProblemaRepository extends JpaRepository<SubcategoriaProblema, Long> {

    List<SubcategoriaProblema> findByAtivoTrueOrderByCategoriaIdAscNomeAsc();

    List<SubcategoriaProblema> findByCategoriaIdAndAtivoTrueOrderByNomeAsc(Long categoriaId);

    // Admin: todas, inclusive as desativadas
    List<SubcategoriaProblema> findAllByOrderByCategoriaIdAscNomeAsc();
}
