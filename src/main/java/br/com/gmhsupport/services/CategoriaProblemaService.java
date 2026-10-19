package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.CategoriaProblemaRequestDTO;
import br.com.gmhsupport.dto.response.CategoriaProblemaResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.CategoriaProblema;
import br.com.gmhsupport.repository.CategoriaProblemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoriaProblemaService {

    private final CategoriaProblemaRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(CategoriaProblemaService.class);

    public CategoriaProblemaService(CategoriaProblemaRepository repository) {
        this.repository = repository;
    }

    public CategoriaProblemaResponseDTO create(CategoriaProblemaRequestDTO dto) {
        logger.info("Criando categoria de problema: {}", dto.nome());

        CategoriaProblema categoriaProblema = new CategoriaProblema();
        categoriaProblema.setNome(dto.nome());
        categoriaProblema.setDescricao(dto.descricao());

        CategoriaProblema createCategoriaProblema = repository.save(categoriaProblema);
        logger.info("Categoria de problema criada com sucesso. ID: {}", createCategoriaProblema.getId());

        return new CategoriaProblemaResponseDTO(createCategoriaProblema);
    }

    public CategoriaProblemaResponseDTO findById(Long id) {
        logger.info("Buscando categoria de problema por id: {}", id);
        if (id == null) {
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        }
        CategoriaProblema findCategoriaProblema = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        return new CategoriaProblemaResponseDTO(findCategoriaProblema);
    }

    public Page<CategoriaProblemaResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todas as categorias de problemas com paginação");
        Page<CategoriaProblema> findCategoriaProblema = repository.findAll(pageable);
        return findCategoriaProblema.map(CategoriaProblemaResponseDTO::new);
    }

    public CategoriaProblemaResponseDTO update(Long id, CategoriaProblemaRequestDTO dto) {
        logger.info("Atualizando categoria de problema ID: {}", id);

        CategoriaProblema updateCategoriaProblema = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        updateCategoriaProblema.setNome(dto.nome());
        updateCategoriaProblema.setDescricao(dto.descricao());

        CategoriaProblema categoriaSalva = repository.save(updateCategoriaProblema);
        logger.info("Categoria de problema ID {} atualizada com sucesso", id);

        return new CategoriaProblemaResponseDTO(categoriaSalva);
    }

    public void delete(Long id) {
        logger.info("Deletando categoria de problema ID: {}", id);

        CategoriaProblema findCategoriaProblema = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        repository.delete(findCategoriaProblema);
        logger.info("Categoria de problema deletada com sucesso");
    }
}