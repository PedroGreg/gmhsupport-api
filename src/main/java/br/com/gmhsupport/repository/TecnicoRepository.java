package br.com.gmhsupport.repository;

import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TecnicoRepository extends BaseRepository<Tecnico>{
    Page<Tecnico> findByEspecialidadeContainingIgnoreCase(String especialidade, Pageable pageable);
}
