package br.com.gmhsupport.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {
    Page<T> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<T> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Optional<T> findByEmailIgnoreCase(String email);
}
