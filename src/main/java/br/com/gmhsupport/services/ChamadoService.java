package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.*;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.dto.response.CodigoConfirmacaoResponseDTO;
import br.com.gmhsupport.dto.response.OrcamentoResponseDTO;
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

import java.math.BigDecimal;
import java.security.SecureRandom;
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
    private final PagamentoService pagamentoService;
    private final PrecoService precoService;
    private final SubcategoriaProblemaRepository subcategoriaRepository;
    private final RepasseService repasseService;

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int MINUTOS_CODIGO = 30;
    private static final int MAX_TENTATIVAS = 5;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          CategoriaProblemaRepository categoriaRepository,
                          StatusChamadoRepository statusRepository,
                          UsuarioRepository usuarioRepository,
                          TecnicoRepository tecnicoRepository, HistoricoChamadoService historicoChamadoService, HistoricoChamadoRepository historicoChamadoRepository,
                          PagamentoService pagamentoService,
                          PrecoService precoService,
                          SubcategoriaProblemaRepository subcategoriaRepository,
                          RepasseService repasseService) {
        this.pagamentoService = pagamentoService;
        this.precoService = precoService;
        this.subcategoriaRepository = subcategoriaRepository;
        this.repasseService = repasseService;
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.statusRepository = statusRepository;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.historicoChamadoService = historicoChamadoService;
        this.historicoChamadoRepository = historicoChamadoRepository;
    }

    @Transactional
    public ChamadoResponseDTO create(ChamadoCreateRequestDTO dto, String emailUsuarioLogado) {
        logger.info("Criando chamado para o usuario: {}", emailUsuarioLogado);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailUsuarioLogado)
            .orElseThrow(() -> new CustomNotFoundException("Usuário não encontrado para o e-mail: " + emailUsuarioLogado));
//        if (usuario == null) {
//            throw new CustomNotFoundException("Usuário não encontrado para o e-mail: " + emailUsuarioLogado);
//        }

        SubcategoriaProblema subcategoria = subcategoriaRepository.findById(dto.subcategoriaId())
                .orElseThrow(() -> new CustomNotFoundException("Subcategoria não encontrada com o ID: " + dto.subcategoriaId()));
        if (!Boolean.TRUE.equals(subcategoria.getAtivo())) {
            throw new IllegalStateException("Este tipo de problema não está mais disponível.");
        }

        Prioridade prioridade = dto.prioridade() != null ? dto.prioridade() : Prioridade.media;

        // Preço: serviço (subcategoria + prioridade) + deslocamento por km. Fora da área: não abre.
        OrcamentoResponseDTO orcamento = precoService.orcar(subcategoria, prioridade, dto.latitude(), dto.longitude());
        if (!orcamento.dentroDaArea()) {
            throw new IllegalStateException("O local fica a " + orcamento.distanciaKm().toPlainString().replace('.', ',')
                    + " km do SENAC Jurubatuba. Atendemos até " + orcamento.distanciaMaximaKm().toPlainString() + " km.");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(prioridade);
        chamado.setUsuario(usuario);
        chamado.setSubcategoria(subcategoria);
        chamado.setCategoria(subcategoria.getCategoria());
        chamado.setStatus(statusRepository.getReferenceById(1L));
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setEnderecoAtendimento(dto.enderecoAtendimento().trim());
        chamado.setComplemento(dto.complemento() != null && !dto.complemento().isBlank() ? dto.complemento().trim() : null);
        chamado.setLatitude(dto.latitude());
        chamado.setLongitude(dto.longitude());
        chamado.setDistanciaKm(orcamento.distanciaKm());

        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        logger.info("O id do chamado criado é: {}", chamadoSalvo.getId());

        // Pagamento na abertura. Se o cartão for inválido, a exceção desfaz também o chamado (@Transactional)
        Pagamento pagamento = pagamentoService.criarNaAbertura(
                chamadoSalvo, orcamento, dto.formaPagamento(), dto.cartaoId(), dto.parcelas());

        String registro = pagamento.getStatus() == StatusPagamento.pago
                ? "Pagamento aprovado: R$ " + PagamentoService.formatar(pagamento.getValor())
                  + " no " + (pagamento.getForma() == FormaPagamento.credito ? "crédito" : "débito")
                  + " final " + pagamento.getCartaoFinal()
                : "Aguardando pagamento do Pix de R$ " + PagamentoService.formatar(pagamento.getValor());
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoSalvo.getId(), null, registro));

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

    @Transactional
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
        limparCodigo(chamado);
        Chamado chamadoCancelado = chamadoRepository.save(chamado);

        // Devolve tudo o que já foi pago (principal e adicional)
        String estorno = pagamentoService.estornarTudo(chamadoCancelado);
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(id, null,
                estorno != null ? "Chamado cancelado pelo cliente. " + estorno : "Chamado cancelado pelo cliente."));

        return new ChamadoResponseDTO(chamadoCancelado);
    }

    public Page<ChamadoResponseDTO> findMeusChamados(String email, Pageable pageable){
        logger.info("Buscando chamados do e-mail {}", email);
        Page<Chamado> findChamados = chamadoRepository.findByUsuarioEmailIgnoreCase(email, pageable);
        return findChamados.map(ChamadoResponseDTO::new);
    }

    public Page<ChamadoResponseDTO> findChamadosAbertos(Pageable pageable){
        logger.info("Buscando chamados abertos");
        // Só chamados Abertos e pagos: cancelados e Pix não confirmado não aparecem na fila
        Page<Chamado> findChamados = chamadoRepository.findFila(pageable);
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

        if (!tecnico.isAprovado()) {
            throw new IllegalStateException("Seu cadastro ainda não foi aprovado pelo administrador.");
        }

        if (chamado.getDataFechamento() != null) {
            throw new IllegalStateException("Não é possível assumir um chamado que já foi finalizado.");
        }

        if (chamado.getTecnico() != null && !chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalStateException("Não é possível assumir um chamado que já está sendo atendido por outro técnico.");
        }

        if (chamado.getStatus() != null && chamado.getStatus().getId() == 5L) {
            throw new IllegalStateException("Este chamado foi cancelado.");
        }

        // Pix ainda não confirmado: o chamado não pode ser atendido
        Pagamento principal = chamado.getPagamentoPrincipal();
        if (principal != null && principal.getStatus() != StatusPagamento.pago) {
            throw new IllegalStateException("Este chamado está aguardando o pagamento do cliente.");
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

    // ===== Finalizar em 2 passos, com código de confirmação =====

    // 1) O técnico descreve a solução. A API gera um código de 6 dígitos que aparece para o CLIENTE.
    @Transactional
    public CodigoConfirmacaoResponseDTO solicitarCodigo(Long chamadoId, String email, SolicitarCodigoRequestDTO dto) {
        logger.info("Gerando código de confirmação do chamado {} para o técnico {}", chamadoId, email);
        Chamado chamado = chamadoDoTecnicoEmAtendimento(chamadoId, email);

        chamado.setSolucao(dto.descricao().trim());
        chamado.setCodigoConfirmacao(String.format("%06d", ALEATORIO.nextInt(1_000_000)));
        chamado.setCodigoExpiraEm(LocalDateTime.now().plusMinutes(MINUTOS_CODIGO));
        chamado.setCodigoTentativas(0);
        chamadoRepository.save(chamado);

        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, chamado.getTecnico().getId(),
                "Técnico concluiu o serviço e pediu o código de confirmação ao cliente."));

        // O técnico NÃO recebe o código: ele precisa pedir ao cliente
        return new CodigoConfirmacaoResponseDTO(null, chamado.getCodigoExpiraEm(), MAX_TENTATIVAS);
    }

    // 2) O técnico digita o código que o cliente falou. Certo = chamado Resolvido e valor liberado.
    @Transactional(noRollbackFor = IllegalStateException.class)
    public ChamadoResponseDTO finalizarChamado(Long chamadoId, String email, ChamadoFinalizarRequestDTO dto){
        logger.info("Finalizando chamado id {} com o tecnico com email {}", chamadoId, email);
        Chamado chamado = chamadoDoTecnicoEmAtendimento(chamadoId, email);

        if (chamado.getCodigoConfirmacao() == null) {
            throw new IllegalStateException("Gere o código de confirmação antes de finalizar.");
        }
        if (!chamado.isCodigoAtivo()) {
            limparCodigo(chamado);
            chamadoRepository.save(chamado);
            throw new IllegalStateException("O código expirou. Gere um novo código.");
        }
        if (!chamado.getCodigoConfirmacao().equals(dto.codigo())) {
            int tentativas = chamado.getCodigoTentativas() + 1;
            chamado.setCodigoTentativas(tentativas);
            if (tentativas >= MAX_TENTATIVAS) {
                limparCodigo(chamado);
                chamadoRepository.save(chamado);
                throw new IllegalStateException("Código incorreto. Limite de tentativas atingido: gere um novo código.");
            }
            chamadoRepository.save(chamado);
            throw new IllegalStateException("Código incorreto. Restam " + (MAX_TENTATIVAS - tentativas) + " tentativas.");
        }

        String solucao = chamado.getSolucao() != null ? chamado.getSolucao() : "Chamado finalizado.";
        limparCodigo(chamado);
        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setStatus(statusRepository.getReferenceById(4L));
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        // 80% de cada pagamento pago fica liberado para o admin repassar ao técnico via Pix
        BigDecimal repasse = repasseService.liberar(chamadoAtualizado);

        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, chamado.getTecnico().getId(), solucao));
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, null,
                "Código confirmado. Atendimento concluído; repasse de R$ " + PagamentoService.formatar(repasse)
                        + " liberado para o técnico."));

        logger.info("Chamado id {} finalizado pelo tecnico id {}", chamadoAtualizado.getId(), chamado.getTecnico().getId());
        return new ChamadoResponseDTO(chamadoAtualizado);
    }

    // GET do cliente: o código que ele deve falar para o técnico
    @Transactional(readOnly = true)
    public CodigoConfirmacaoResponseDTO codigoDoCliente(Long chamadoId, String email) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));
        if (chamado.getUsuario() == null || !chamado.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Somente o dono do chamado pode ver o código.");
        }
        if (!chamado.isCodigoAtivo()) {
            throw new CustomNotFoundException("Não há código de confirmação ativo para este chamado.");
        }
        return new CodigoConfirmacaoResponseDTO(chamado.getCodigoConfirmacao(), chamado.getCodigoExpiraEm(),
                MAX_TENTATIVAS - chamado.getCodigoTentativas());
    }

    private Chamado chamadoDoTecnicoEmAtendimento(Long chamadoId, String email) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException(chamadoId));
        Tecnico tecnico = tecnicoRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o e-mail: " + email));

        if (chamado.getTecnico() == null || !chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalStateException("Apenas o técnico responsável pode finalizar este chamado. Assuma o chamado primeiro.");
        }
        Long statusAtual = chamado.getStatus() != null ? chamado.getStatus().getId() : null;
        if (statusAtual != null && statusAtual == 6L) {
            throw new IllegalStateException("Aguarde o cliente responder o pedido de adicional antes de finalizar.");
        }
        if (statusAtual == null || (statusAtual != 2L && statusAtual != 3L)) {
            throw new IllegalStateException("Só é possível finalizar chamados em atendimento ou aguardando usuário.");
        }
        return chamado;
    }

    private void limparCodigo(Chamado chamado) {
        chamado.setCodigoConfirmacao(null);
        chamado.setCodigoExpiraEm(null);
        chamado.setCodigoTentativas(0);
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
                // Só faz sentido aguardar o usuário quando o chamado está em atendimento
                if (chamado.getStatus() == null || !chamado.getStatus().getId().equals(2L)) {
                    throw new IllegalStateException("Só é possível aguardar o usuário em chamados em atendimento.");
                }
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