package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    // Principal primeiro, depois os mais novos
    List<Cartao> findByUsuarioIdOrderByPrincipalDescIdDesc(Long usuarioId);

    Optional<Cartao> findByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}
