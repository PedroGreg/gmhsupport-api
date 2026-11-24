package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.StatusCadastro;
import br.com.gmhsupport.model.Tecnico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TecnicoRepository extends BaseRepository<Tecnico>{
    Page<Tecnico> findByEspecialidadeContainingIgnoreCase(String especialidade, Pageable pageable);

    boolean existsByCpf(String cpf);

    // Lista do admin: os mais antigos primeiro (fila de análise)
    List<Tecnico> findByStatusCadastroOrderByDataCadastroAsc(StatusCadastro status);

    List<Tecnico> findAllByOrderByDataCadastroDesc();

    long countByStatusCadastro(StatusCadastro status);
}
