package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.CategoriaProblemaRequestDTO;
import br.com.gmhsupport.dto.response.CategoriaProblemaResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.CategoriaProblema;
import br.com.gmhsupport.dto.response.CategoriaComSubcategoriasDTO;
import br.com.gmhsupport.dto.response.SubcategoriaResponseDTO;
import br.com.gmhsupport.repository.CategoriaProblemaRepository;
import br.com.gmhsupport.repository.SubcategoriaProblemaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoriaProblemaService {

    private final CategoriaProblemaRepository repository;
    private final SubcategoriaProblemaRepository subcategoriaRepository;

    private static final Logger logger = LoggerFactory.getLogger(CategoriaProblemaService.class);

    public CategoriaProblemaService(CategoriaProblemaRepository repository,
                                    SubcategoriaProblemaRepository subcategoriaRepository) {
        this.repository = repository;
        this.subcategoriaRepository = subcategoriaRepository;
    }

    // Todas as categorias, cada uma com os seus problemas (subcategorias ativas) e preços
    @Transactional(readOnly = true)
    public List<CategoriaComSubcategoriasDTO> arvore() {
        Map<Long, List<SubcategoriaResponseDTO>> porCategoria = subcategoriaRepository
                .findByAtivoTrueOrderByCategoriaIdAscNomeAsc().stream()
                .map(SubcategoriaResponseDTO::new)
                .collect(Collectors.groupingBy(SubcategoriaResponseDTO::categoriaId, LinkedHashMap::new, Collectors.toList()));

        return repository.findAll(Sort.by("id")).stream()
                .filter(c -> porCategoria.containsKey(c.getId()))
                .map(c -> new CategoriaComSubcategoriasDTO(c.getId(), c.getNome(), c.getDescricao(),
                        c.getValorFixo(), porCategoria.get(c.getId())))
                .toList();
    }

    public CategoriaProblemaResponseDTO create(CategoriaProblemaRequestDTO dto) {
        logger.info("Criando categoria de problema: {}", dto.nome());

        CategoriaProblema categoriaProblema = new CategoriaProblema();
        categoriaProblema.setNome(dto.nome());
        categoriaProblema.setDescricao(dto.descricao());
        if (dto.valorFixo() != null) categoriaProblema.setValorFixo(dto.valorFixo());

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
        if (dto.valorFixo() != null) updateCategoriaProblema.setValorFixo(dto.valorFixo());

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