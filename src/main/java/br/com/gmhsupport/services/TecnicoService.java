package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.TecnicoRequestDTO;
import br.com.gmhsupport.dto.response.TecnicoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;
    private final PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(TecnicoService.class);
    public TecnicoService(TecnicoRepository repository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public TecnicoResponseDTO create(TecnicoRequestDTO dto){
        logger.info("Criando tecnico");
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(dto.nome());
        tecnico.setEmail(dto.email().toLowerCase().trim());
        tecnico.setSenha(passwordEncoder.encode(dto.senha()));
        tecnico.setEspecialidade(dto.especialidade());
        tecnico.setDataCadastro(LocalDateTime.now());

        Tecnico createTecnico = repository.save(tecnico);
        logger.info("O id do tecnico é: " + createTecnico.getId());
        return new TecnicoResponseDTO(createTecnico);
    }

    public TecnicoResponseDTO findById(Long id) {
        logger.info("Buscando tecnico por id");
        if(id == null)
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        Tecnico findTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        logger.info("O id do tecnico é: " + id);
        return new TecnicoResponseDTO(findTecnico);
    }

    public Page<TecnicoResponseDTO> findAll(Pageable pageable){
        logger.info("Buscando todos tecnicos");
        Page<Tecnico> findTecnico = repository.findAll(pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public Page<TecnicoResponseDTO> findByNome(String nome, Pageable pageable){
        logger.info("Buscando todos tecnicos com o nome informado");
        Page<Tecnico> findTecnico = repository.findByNomeContainingIgnoreCase(nome, pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public Page<TecnicoResponseDTO> findByEmail(String email, Pageable pageable){
        logger.info("Buscando todos tecnicos com o email informado");
        Page<Tecnico> findTecnico = repository.findByEmailContainingIgnoreCase(email, pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public TecnicoResponseDTO update(TecnicoRequestDTO dto, Long id){
        logger.info("Atualizando tecnico");
        Tecnico updateTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        updateTecnico.setNome(dto.nome());
        updateTecnico.setEmail(dto.email().toLowerCase().trim());
        updateTecnico.setSenha(passwordEncoder.encode(dto.senha()));
        updateTecnico.setEspecialidade(dto.especialidade());

        updateTecnico = repository.save(updateTecnico);
        logger.info("O id do tecnico é: " + id);
        return new TecnicoResponseDTO(updateTecnico);
    }

    public void delete(Long id){
        logger.info("Deletando tecnico");
        Tecnico findTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        repository.delete(findTecnico);
        logger.info("Tecnico deletado");
    }
}