package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Adicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdicionalRepository extends JpaRepository<Adicional, Long> {

    Optional<Adicional> findByChamadoId(Long chamadoId);
}
