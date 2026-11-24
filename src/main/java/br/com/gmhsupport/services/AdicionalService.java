package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.AdicionalAceitarRequestDTO;
import br.com.gmhsupport.dto.request.AdicionalDecisaoRequestDTO;
import br.com.gmhsupport.dto.request.AdicionalRequestDTO;
import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.response.ChamadoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.*;
import br.com.gmhsupport.repository.AdicionalRepository;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.StatusChamadoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Valor adicional pedido pelo técnico (serviço mais complexo ou diferente do solicitado).
// Fluxo:
//  1. Técnico pede (status 2 ou 3)  -> chamado vai para 6 "Aguardando aprovação do adicional"
//  2a. Cliente aceita e paga         -> volta para 2 "Em andamento" (no Pix, depois da confirmação)
//  2b. Cliente recusa                -> volta para 2 e o técnico decide:
//        continuar = segue pelo valor original · cancelar = encerra e estorna tudo ao cliente
// Limite: 1 adicional por chamado.
@Service
public class AdicionalService {

    private static final Logger logger = LoggerFactory.getLogger(AdicionalService.class);

    private final AdicionalRepository adicionalRepository;
    private final ChamadoRepository chamadoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final StatusChamadoRepository statusRepository;
    private final PagamentoService pagamentoService;
    private final HistoricoChamadoService historicoChamadoService;

    public AdicionalService(AdicionalRepository adicionalRepository, ChamadoRepository chamadoRepository,
                            TecnicoRepository tecnicoRepository, StatusChamadoRepository statusRepository,
                            PagamentoService pagamentoService, HistoricoChamadoService historicoChamadoService) {
        this.adicionalRepository = adicionalRepository;
        this.chamadoRepository = chamadoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.statusRepository = statusRepository;
        this.pagamentoService = pagamentoService;
        this.historicoChamadoService = historicoChamadoService;
    }

    // 1. Técnico pede o adicional
    @Transactional
    public ChamadoResponseDTO solicitar(Long chamadoId, String emailTecnico, AdicionalRequestDTO dto) {
        Chamado chamado = buscarChamado(chamadoId);
        Tecnico tecnico = tecnicoResponsavel(chamado, emailTecnico);

        long status = statusId(chamado);
        if (status != 2L && status != 3L) {
            throw new IllegalStateException("Só é possível pedir adicional em chamados em atendimento.");
        }
        if (adicionalRepository.findByChamadoId(chamadoId).isPresent()) {
            throw new IllegalStateException("Este chamado já teve um pedido de adicional (limite de 1 por chamado).");
        }

        Adicional adicional = new Adicional();
        adicional.setChamado(chamado);
        adicional.setTecnico(tecnico);
        adicional.setValor(dto.valor());
        adicional.setMotivo(dto.motivo().trim());
        adicional.setStatus(StatusAdicional.pendente);
        adicionalRepository.save(adicional);
        chamado.setAdicional(adicional);

        // Um código de finalização gerado antes deixa de valer
        chamado.setCodigoConfirmacao(null);
        chamado.setCodigoExpiraEm(null);
        chamado.setStatus(statusRepository.getReferenceById(6L));
        chamadoRepository.save(chamado);

        registrar(chamadoId, tecnico.getId(), "Técnico pediu um adicional de R$ " + PagamentoService.formatar(dto.valor())
                + ". Motivo: " + adicional.getMotivo());
        logger.info("Adicional pedido no chamado {} pelo técnico {}", chamadoId, tecnico.getId());
        return new ChamadoResponseDTO(chamado);
    }

    // 2a. Cliente aceita e escolhe como pagar
    @Transactional
    public ChamadoResponseDTO aceitar(Long chamadoId, String emailCliente, AdicionalAceitarRequestDTO dto) {
        Chamado chamado = buscarChamado(chamadoId);
        validarDono(chamado, emailCliente);
        Adicional adicional = adicionalPendente(chamado);

        adicional.setStatus(StatusAdicional.aceito);
        adicional.setDataResposta(LocalDateTime.now());
        adicionalRepository.save(adicional);
        registrar(chamadoId, null, "Cliente aceitou o adicional de R$ " + PagamentoService.formatar(adicional.getValor()) + ".");

        // Fica em 6 (aguardando) até o Mercado Pago aprovar; aí o PagamentoService volta o atendimento para 2
        pagamentoService.criarAdicional(chamado, adicional.getValor(), dto.formaPagamento());
        return new ChamadoResponseDTO(chamado);
    }

    // 2b. Cliente recusa
    @Transactional
    public ChamadoResponseDTO recusar(Long chamadoId, String emailCliente) {
        Chamado chamado = buscarChamado(chamadoId);
        validarDono(chamado, emailCliente);
        Adicional adicional = adicionalPendente(chamado);

        adicional.setStatus(StatusAdicional.recusado);
        adicional.setDataResposta(LocalDateTime.now());
        adicionalRepository.save(adicional);

        chamado.setStatus(statusRepository.getReferenceById(2L));
        chamadoRepository.save(chamado);
        registrar(chamadoId, null, "Cliente recusou o adicional. Aguardando a decisão do técnico.");
        return new ChamadoResponseDTO(chamado);
    }

    // 3. Depois da recusa, o técnico decide
    @Transactional
    public ChamadoResponseDTO decidir(Long chamadoId, String emailTecnico, AdicionalDecisaoRequestDTO dto) {
        Chamado chamado = buscarChamado(chamadoId);
        Tecnico tecnico = tecnicoResponsavel(chamado, emailTecnico);
        Adicional adicional = adicionalRepository.findByChamadoId(chamadoId)
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não tem pedido de adicional."));

        if (adicional.getStatus() != StatusAdicional.recusado) {
            throw new IllegalStateException("A decisão só existe quando o cliente recusa o adicional.");
        }
        if (adicional.getDecisaoTecnico() != null) {
            throw new IllegalStateException("A decisão já foi tomada.");
        }
        long status = statusId(chamado);
        if (status != 2L && status != 3L) {
            throw new IllegalStateException("O chamado não está mais em atendimento.");
        }

        adicional.setDecisaoTecnico(dto.decisao());
        adicionalRepository.save(adicional);

        if (dto.decisao() == DecisaoAdicional.continuar) {
            registrar(chamadoId, tecnico.getId(), "Técnico vai continuar o atendimento pelo valor original.");
        } else {
            chamado.setStatus(statusRepository.getReferenceById(5L));
            chamado.setDataFechamento(LocalDateTime.now());
            chamado.setCodigoConfirmacao(null);
            chamado.setCodigoExpiraEm(null);
            chamadoRepository.save(chamado);
            String estorno = pagamentoService.estornarTudo(chamado);
            registrar(chamadoId, tecnico.getId(), "Técnico cancelou o atendimento após a recusa do adicional."
                    + (estorno != null ? " " + estorno : ""));
        }
        return new ChamadoResponseDTO(chamado);
    }

    // ----- apoio -----

    private Chamado buscarChamado(Long id) {
        return chamadoRepository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
    }

    private Tecnico tecnicoResponsavel(Chamado chamado, String email) {
        Tecnico tecnico = tecnicoRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o e-mail: " + email));
        if (chamado.getTecnico() == null || !chamado.getTecnico().getId().equals(tecnico.getId())) {
            throw new AccessDeniedException("Apenas o técnico responsável pode fazer isso.");
        }
        return tecnico;
    }

    private void validarDono(Chamado chamado, String email) {
        if (chamado.getUsuario() == null || !chamado.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Somente o dono do chamado pode responder o adicional.");
        }
    }

    private Adicional adicionalPendente(Chamado chamado) {
        Adicional adicional = adicionalRepository.findByChamadoId(chamado.getId())
                .orElseThrow(() -> new CustomNotFoundException("Este chamado não tem pedido de adicional."));
        if (adicional.getStatus() != StatusAdicional.pendente || statusId(chamado) != 6L) {
            throw new IllegalStateException("Este pedido de adicional já foi respondido.");
        }
        return adicional;
    }

    private long statusId(Chamado chamado) {
        return chamado.getStatus() != null ? chamado.getStatus().getId() : 0L;
    }

    private void registrar(Long chamadoId, Long tecnicoId, String texto) {
        historicoChamadoService.create(new HistoricoChamadoRequestDTO(chamadoId, tecnicoId, texto));
    }
}
