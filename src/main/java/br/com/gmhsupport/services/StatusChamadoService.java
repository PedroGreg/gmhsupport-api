package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.StatusChamadoRequestDTO;
import br.com.gmhsupport.dto.response.StatusChamadoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.StatusChamado;
import br.com.gmhsupport.repository.StatusChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StatusChamadoService {

    private final StatusChamadoRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(StatusChamadoService.class);

    public StatusChamadoService(StatusChamadoRepository repository) {
        this.repository = repository;
    }

    public StatusChamadoResponseDTO create(StatusChamadoRequestDTO dto) {
        logger.info("Criando status de chamado: {}", dto.descricao());

        StatusChamado statusChamado = new StatusChamado();
        statusChamado.setDescricao(dto.descricao());

        StatusChamado createStatus = repository.save(statusChamado);
        logger.info("Status de chamado criado com sucesso. ID: {}", createStatus.getId());

        return new StatusChamadoResponseDTO(createStatus);
    }

    public StatusChamadoResponseDTO findById(Long id) {
        logger.info("Buscando status de chamado por id: {}", id);
        if (id == null) {
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        }
        StatusChamado statusChamado = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        return new StatusChamadoResponseDTO(statusChamado);
    }

    public Page<StatusChamadoResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todos os status de chamado com paginação");
        Page<StatusChamado> findStatus = repository.findAll(pageable);
        return findStatus.map(StatusChamadoResponseDTO::new);
    }

    public StatusChamadoResponseDTO update(Long id, StatusChamadoRequestDTO dto) {
        logger.info("Atualizando status de chamado ID: {}", id);

        StatusChamado updateStatus = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        updateStatus.setDescricao(dto.descricao());

        StatusChamado statusSalvo = repository.save(updateStatus);
        logger.info("Status de chamado ID {} atualizado com sucesso", id);

        return new StatusChamadoResponseDTO(statusSalvo);
    }

    public void delete(Long id) {
        logger.info("Deletando status de chamado ID: {}", id);

        StatusChamado statusChamado = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        repository.delete(statusChamado);
        logger.info("Status de chamado deletado com sucesso");
    }
}