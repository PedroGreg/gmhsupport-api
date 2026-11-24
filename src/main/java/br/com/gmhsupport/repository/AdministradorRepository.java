package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByEmailIgnoreCase(String email);
}
