package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.*;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.dto.response.HistoricoChamadoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaProblemaRepository categoriaRepository;
    private final StatusChamadoRepository statusRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    private static final Logger logger = LoggerFactory.getLogger(ChamadoService.class);
    private final HistoricoChamadoService historicoChamadoService;
    private final HistoricoChamadoRepository historicoChamadoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          CategoriaProblemaRepository categoriaRepository,
                          StatusChamadoRepository statusRepository,
                          UsuarioRepository usuarioRepository,
                          TecnicoRepository tecnicoRepository, HistoricoChamadoService historicoChamadoService, HistoricoChamadoRepository historicoChamadoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.statusRepository = statusRepository;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.historicoChamadoService = historicoChamadoService;
        this.historicoChamadoRepository = historicoChamadoRepository;
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

    @Transactional
    public HistoricoChamadoResponseDTO adicionarMensagem(Long id, String email, ChamadoMensagemRequestDTO dto){
        logger.info("Adicionando mensagem no histórico do chamado ID {} pelo e-mail: {}", id, email);

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        Optional<Tecnico> tecnico = tecnicoRepository.findByEmailIgnoreCase(email);
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(email);

        Usuario usuarioAutor = null;
        Tecnico tecnicoAutor = null;

        if(usuario.isPresent()){
            usuarioAutor = usuario.get();
            if(chamado.getUsuario() == null || !chamado.getUsuario().getId().equals(usuarioAutor.getId())){
                throw new AccessDeniedException("Apenas o dono do chamado pode enviar mensagens neste atendimento.");
            }

            if (chamado.getStatus() != null && chamado.getStatus().getId().equals(3L)){
                chamado.setStatus(statusRepository.getReferenceById(2L));
                chamadoRepository.save(chamado);
                logger.info("Status do chamado ID {} alterado de 'Aguardando usuário' para 'Em andamento'", id);
            }
        } else if (tecnico.isPresent()) {
            tecnicoAutor = tecnico.get();
            if (chamado.getTecnico() == null || !chamado.getTecnico().getId().equals(tecnicoAutor.getId())) {
                throw new AccessDeniedException("Apenas o técnico responsável pode registrar evoluções neste chamado.");
            }
        }
        else {
            throw new CustomNotFoundException("Usuário/Técnico não encontrado com o e-mail: " + email);
        }

        HistoricoChamado historicoChamado = new HistoricoChamado();
        historicoChamado.setChamado(chamado);
        historicoChamado.setUsuario(usuarioAutor);
        historicoChamado.setTecnico(tecnicoAutor);
        historicoChamado.setDescricao(dto.descricao());
        historicoChamado.setDataRegistro(LocalDateTime.now());

        HistoricoChamado historicoSalvo = historicoChamadoRepository.save(historicoChamado);
        logger.info("Mensagem registrada com sucesso no histórico ID {}", historicoSalvo.getId());

        return new HistoricoChamadoResponseDTO(historicoSalvo);
    }

    // Busca o chamado conferindo se quem pediu pode vê-lo
    public ChamadoResponseDTO findByIdComAcesso(Long id, String email) {
        logger.info("Buscando chamado id {} para o e-mail {}", id, email);
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        validarAcesso(chamado, email);
        return new ChamadoResponseDTO(chamado);
    }

    // Usado também pelo histórico: técnico vê qualquer chamado; usuário só os próprios
    public void validarAcesso(Long chamadoId, String email) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));
        validarAcesso(chamado, email);
    }

    private void validarAcesso(Chamado chamado, String email) {
        if (tecnicoRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        if (chamado.getUsuario() == null || !chamado.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Você não tem acesso a este chamado.");
        }
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

    public ChamadoResponseDTO cancelarChamado(String email, Long id){
        logger.info("Cancelando chamado com o id {}", id);
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException(email));

        if(chamado.getUsuario() != null && !chamado.getUsuario().getId().equals(usuario.getId())){
            throw new IllegalStateException("Não é possível cancelar um chamado de outro usuário.");
        }

        if (chamado.getStatus() != null && (chamado.getStatus().getId() == 4L || chamado.getStatus().getId() == 5L)) {
            throw new IllegalStateException("Este chamado já foi finalizado ou cancelado.");
        }

        chamado.setStatus(statusRepository.getReferenceById(5L));
        chamado.setDataFechamento(LocalDateTime.now());
        Chamado chamadoCancelado = chamadoRepository.save(chamado);
        return new ChamadoResponseDTO(chamadoCancelado);
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

    public ChamadoResponseDTO aguardandoUsuario(Long id, String email){
        logger.info("Atualizando status do chamado id {} para aguardando usuario", id);

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        Optional<Tecnico> tecnico = tecnicoRepository.findByEmailIgnoreCase(email);
        if(tecnico.isPresent()) {
            if (chamado.getTecnico() != null && chamado.getTecnico().getId().equals(tecnico.get().getId())) {
                chamado.setStatus(statusRepository.getReferenceById(3L));
            }
            else {
                throw new AccessDeniedException("Somente o técnico responsavel ou admin pode alterar o status do chamado");
            }
        }
        else{
            throw new AccessDeniedException("Somente o técnico responsavel ou admin pode alterar o status do chamado");
        }

        Chamado chamadoAtualizado = chamadoRepository.save(chamado);

        HistoricoChamado historicoChamado = new HistoricoChamado();
        historicoChamado.setUsuario(null);
        historicoChamado.setChamado(chamado);
        historicoChamado.setTecnico(tecnico.get());
        historicoChamado.setDescricao("Aguardando usuário");
        historicoChamado.setDataRegistro(LocalDateTime.now());
        historicoChamadoRepository.save(historicoChamado);

        return new ChamadoResponseDTO(chamadoAtualizado);
    }

    public void delete(Long id) {
        logger.info("Deletando chamado ID: {}", id);
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        chamadoRepository.delete(chamado);
        logger.info("Chamado deletado");
    }
}