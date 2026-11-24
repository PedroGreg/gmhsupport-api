package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.SubcategoriaAdminRequestDTO;
import br.com.gmhsupport.dto.response.AdminResumoResponseDTO;
import br.com.gmhsupport.dto.response.DocumentoInfoDTO;
import br.com.gmhsupport.dto.response.SubcategoriaAdminResponseDTO;
import br.com.gmhsupport.dto.response.TecnicoAdminResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Área do administrador: análise de técnicos, resumo e preços dos problemas
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final TecnicoRepository tecnicoRepository;
    private final DocumentoTecnicoRepository documentoRepository;
    private final AdministradorRepository administradorRepository;
    private final PagamentoRepository pagamentoRepository;
    private final SubcategoriaProblemaRepository subcategoriaRepository;
    private final CategoriaProblemaRepository categoriaRepository;

    public AdminService(TecnicoRepository tecnicoRepository, DocumentoTecnicoRepository documentoRepository,
                        AdministradorRepository administradorRepository, PagamentoRepository pagamentoRepository,
                        SubcategoriaProblemaRepository subcategoriaRepository,
                        CategoriaProblemaRepository categoriaRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.documentoRepository = documentoRepository;
        this.administradorRepository = administradorRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public AdminResumoResponseDTO resumo(String email) {
        Administrador admin = administradorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Administrador não encontrado"));
        BigDecimal aPagar = pagamentoRepository.somarRepasses(StatusRepasse.liberado);
        BigDecimal pago = pagamentoRepository.somarRepasses(StatusRepasse.pago);
        return new AdminResumoResponseDTO(admin.getNome(), admin.getEmail(),
                tecnicoRepository.countByStatusCadastro(StatusCadastro.pendente),
                pagamentoRepository.contarRepasses(StatusRepasse.liberado),
                aPagar != null ? aPagar : BigDecimal.ZERO,
                pago != null ? pago : BigDecimal.ZERO);
    }

    // ===== Técnicos =====

    // status = null: todos (mais novos primeiro); senão só os daquele status (mais antigos primeiro)
    @Transactional(readOnly = true)
    public List<TecnicoAdminResponseDTO> listarTecnicos(StatusCadastro status) {
        Map<Long, DocumentoInfoDTO> documentos = documentoRepository.listarInfos().stream()
                .collect(Collectors.toMap(DocumentoInfoDTO::tecnicoId, Function.identity(), (a, b) -> a));
        List<Tecnico> tecnicos = status == null
                ? tecnicoRepository.findAllByOrderByDataCadastroDesc()
                : tecnicoRepository.findByStatusCadastroOrderByDataCadastroAsc(status);
        return tecnicos.stream().map(t -> new TecnicoAdminResponseDTO(t, documentos.get(t.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public TecnicoAdminResponseDTO buscarTecnico(Long id) {
        Tecnico t = tecnico(id);
        return new TecnicoAdminResponseDTO(t, documentoRepository.infoDoTecnico(id).orElse(null));
    }

    @Transactional(readOnly = true)
    public DocumentoTecnico documento(Long tecnicoId) {
        return documentoRepository.findByTecnicoId(tecnicoId)
                .orElseThrow(() -> new CustomNotFoundException("Este técnico não enviou a certidão."));
    }

    @Transactional
    public TecnicoAdminResponseDTO aprovar(Long id, String emailAdmin) {
        Tecnico t = tecnico(id);
        if (t.getStatusCadastro() == StatusCadastro.aprovado) {
            throw new IllegalStateException("Este técnico já está aprovado.");
        }
        t.setStatusCadastro(StatusCadastro.aprovado);
        t.setMotivoRecusa(null);
        t.setDataAnalise(LocalDateTime.now());
        tecnicoRepository.save(t);
        logger.info("Admin {} aprovou o técnico {}", emailAdmin, id);
        return buscarTecnico(id);
    }

    @Transactional
    public TecnicoAdminResponseDTO recusar(Long id, String motivo, String emailAdmin) {
        Tecnico t = tecnico(id);
        t.setStatusCadastro(StatusCadastro.recusado);
        t.setMotivoRecusa(motivo.trim());
        t.setDataAnalise(LocalDateTime.now());
        tecnicoRepository.save(t);
        logger.info("Admin {} recusou o técnico {}", emailAdmin, id);
        return buscarTecnico(id);
    }

    private Tecnico tecnico(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado: " + id));
    }

    // ===== Preços (subcategorias) =====

    @Transactional(readOnly = true)
    public List<SubcategoriaAdminResponseDTO> listarSubcategorias() {
        return subcategoriaRepository.findAllByOrderByCategoriaIdAscNomeAsc().stream()
                .map(SubcategoriaAdminResponseDTO::new).toList();
    }

    @Transactional
    public SubcategoriaAdminResponseDTO criarSubcategoria(SubcategoriaAdminRequestDTO dto) {
        SubcategoriaProblema s = new SubcategoriaProblema();
        preencher(s, dto);
        if (s.getAtivo() == null) s.setAtivo(true);
        return new SubcategoriaAdminResponseDTO(subcategoriaRepository.save(s));
    }

    @Transactional
    public SubcategoriaAdminResponseDTO atualizarSubcategoria(Long id, SubcategoriaAdminRequestDTO dto) {
        SubcategoriaProblema s = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Problema não encontrado: " + id));
        preencher(s, dto);
        return new SubcategoriaAdminResponseDTO(subcategoriaRepository.save(s));
    }

    private void preencher(SubcategoriaProblema s, SubcategoriaAdminRequestDTO dto) {
        CategoriaProblema categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new CustomNotFoundException("Categoria não encontrada: " + dto.categoriaId()));
        // Na categoria Testes (valor fixo) o Mercado Pago exige no mínimo R$ 5,01 no Pix de teste
        if (Boolean.TRUE.equals(categoria.getValorFixo()) && dto.valorBase().compareTo(new BigDecimal("5.01")) < 0) {
            throw new IllegalArgumentException("Na categoria " + categoria.getNome() + " o valor mínimo é R$ 5,01.");
        }
        s.setCategoria(categoria);
        s.setNome(dto.nome().trim());
        s.setDescricao(dto.descricao() != null && !dto.descricao().isBlank() ? dto.descricao().trim() : null);
        s.setValorBase(dto.valorBase().setScale(2, RoundingMode.HALF_UP));
        if (dto.ativo() != null) s.setAtivo(dto.ativo());
    }
}
