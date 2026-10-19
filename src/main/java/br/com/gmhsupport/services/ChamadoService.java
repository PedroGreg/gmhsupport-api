package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.ChamadoCreateRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoFinalizarRequestDTO;
import br.com.gmhsupport.dto.request.ChamadoRequestDTO;
import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.dto.response.HistoricoChamadoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.CategoriaProblemaRepository;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.StatusChamadoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import br.com.gmhsupport.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaProblemaRepository categoriaRepository;
    private final StatusChamadoRepository statusRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    private static final Logger logger = LoggerFactory.getLogger(ChamadoService.class);
    private final HistoricoChamadoService historicoChamadoService;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          CategoriaProblemaRepository categoriaRepository,
                          StatusChamadoRepository statusRepository,
                          UsuarioRepository usuarioRepository,
                          TecnicoRepository tecnicoRepository, HistoricoChamadoService historicoChamadoService) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.statusRepository = statusRepository;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.historicoChamadoService = historicoChamadoService;
    }

    public ChamadoResponseDTO create(ChamadoCreateRequestDTO dto, String emailUsuarioLogado) {
        logger.info("Criando chamado para o usuario: {}", emailUsuarioLogado);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailUsuarioLogado)
            .orElseThrow(() -> new CustomNotFoundException("Usuário não encontrado para o e-mail: " + emailUsuarioLogado));
//        if (usuario == null) {
//            throw new CustomNotFoundException("Usuário não encontrado para o e-mail: " + emailUsuarioLogado);
//        }

        CategoriaProblema categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new CustomNotFoundException("Categoria não encontrada com o ID: " + dto.categoriaId()));

        StatusChamado statusInicial = statusRepository.getReferenceById(1L);

        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setUsuario(usuario);
        chamado.setCategoria(categoria);
        chamado.setStatus(statusInicial);
        chamado.setDataAbertura(LocalDateTime.now());

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        logger.info("O id do chamado criado é: {}", chamadoSalvo.getId());

        return new ChamadoResponseDTO(chamadoSalvo);
    }

    public ChamadoResponseDTO findById(Long id) {
        logger.info("Buscando chamado por id: {}", id);
        if (id == null) {
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        }
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        return new ChamadoResponseDTO(chamado);
    }

    public Page<ChamadoResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todos chamados");
        Page<Chamado> chamados = chamadoRepository.findAll(pageable);
        return chamados.map(ChamadoResponseDTO::new);
    }

    public Page<ChamadoResponseDTO> findMeusChamados(String email, Pageable pageable){
        logger.info("Buscando chamados do e-mail {}", email);
        Page<Chamado> findChamados = chamadoRepository.findByUsuarioEmailIgnoreCase(email, pageable);
        return findChamados.map(ChamadoResponseDTO::new);
    }

    public Page<ChamadoResponseDTO> findChamadosAbertos(Pageable pageable){
        logger.info("Buscando chamados abertos");
        Page<Chamado> findChamados = chamadoRepository.findByTecnicoIsNull(pageable);
        return findChamados.map(ChamadoResponseDTO::new);
    }

    public Page<ChamadoResponseDTO> findChamadosAtribuidos(Pageable pageable){
        logger.info("Buscando chamados atribuidos");
        Page<Chamado> findChamados = chamadoRepository.findByTecnicoIsNotNull(pageable);
        return findChamados.map(ChamadoResponseDTO::new);
    }

    public Page<ChamadoResponseDTO> findChamadosAtribuidos(String email, Pageable pageable){
        logger.info("Buscando chamados atribuidos para o e-mail {}", email);
        Page<Chamado> findChamados = chamadoRepository.findByTecnicoEmailIgnoreCase(email, pageable);
        return findChamados.map(ChamadoResponseDTO::new);
    }

    public ChamadoResponseDTO assumirChamado(Long chamadoId, String email){
        logger.info("Assumindo chamado id {} para o tecnico com email {}", chamadoId, email);
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));

        Tecnico tecnico = tecnicoRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o e-mail: " + email));

        if (chamado.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível assumir um chamado que já foi finalizado.");
        }

        if (chamado.getTecnico() != null && !chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalStateException("Não é possível assumir um chamado que já está sendo atendido por outro técnico.");
        }

        chamado.setDataAtendimento(LocalDateTime.now());
        chamado.setStatus(statusRepository.getReferenceById(2L));
        chamado.setTecnico(tecnico);

        HistoricoChamadoRequestDTO historico = new HistoricoChamadoRequestDTO(
                chamadoId,
                tecnico.getId(),
                "Atendimento iniciado pelo técnico."
        );
        historicoChamadoService.create(historico);

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        logger.info("Chamado id {} designado para o tecnico id {}",chamadoAtualizado.getId(), tecnico.getId());
        return new ChamadoResponseDTO(chamadoAtualizado);
    }

    public ChamadoResponseDTO finalizarChamado(Long chamadoId, String email, ChamadoFinalizarRequestDTO dto){
        logger.info("Finalizando chamado id {} com o tecnico com email {}", chamadoId, email);

        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));

        Tecnico tecnico = tecnicoRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o e-mail: " + email));

        if (chamado.getTecnico() != null && !chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalStateException("Apenas o técnico responsável pode finalizar este chamado.");
        }

        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setTecnico(tecnico);
        chamado.setStatus(statusRepository.getReferenceById(4L));
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        HistoricoChamadoRequestDTO historico = new HistoricoChamadoRequestDTO(chamadoId, tecnico.getId(), dto.descricao());
        historicoChamadoService.create(historico);

        logger.info("Chamado id {} designado para o tecnico id {}", chamadoAtualizado.getId(), tecnico.getId());
        return new ChamadoResponseDTO(chamadoAtualizado);
    }

    public ChamadoResponseDTO update(Long id, ChamadoRequestDTO dto) {
        logger.info("Atualizando chamado ID: {}", id);

        // 1. Busca o chamado existente
        Chamado updateChamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        // 2. Busca e atualiza a categoria
        if (dto.categoriaId() != null) {
            CategoriaProblema categoria = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new CustomNotFoundException("Categoria não encontrada com o ID: " + dto.categoriaId()));
            updateChamado.setCategoria(categoria);
        }

        // 3. Atualiza dados básicos
        updateChamado.setTitulo(dto.titulo());
        updateChamado.setDescricao(dto.descricao());
        updateChamado.setPrioridade(dto.prioridade());

        if (dto.tecnicoId() != null) {
            Tecnico tecnico = tecnicoRepository.findById(dto.tecnicoId())
                    .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o ID: " + dto.tecnicoId()));
            updateChamado.setTecnico(tecnico);

            if (updateChamado.getDataAtendimento() == null) {
                updateChamado.setDataAtendimento(LocalDateTime.now());
            }
        }

        Chamado chamadoSalvo = chamadoRepository.save(updateChamado);
        logger.info("Chamado ID {} atualizado com sucesso", id);

        return new ChamadoResponseDTO(chamadoSalvo);
    }

    public void delete(Long id) {
        logger.info("Deletando chamado ID: {}", id);
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        chamadoRepository.delete(chamado);
        logger.info("Chamado deletado");
    }
}