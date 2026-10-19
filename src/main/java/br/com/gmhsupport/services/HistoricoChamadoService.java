package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.HistoricoChamadoRequestDTO;
import br.com.gmhsupport.dto.response.HistoricoChamadoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.HistoricoChamado;
import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.HistoricoChamadoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoricoChamadoService {

    private final HistoricoChamadoRepository historicoRepository;
    private final ChamadoRepository chamadoRepository;
    private final TecnicoRepository tecnicoRepository;

    private static final Logger logger = LoggerFactory.getLogger(HistoricoChamadoService.class);

    public HistoricoChamadoService(HistoricoChamadoRepository historicoRepository,
                                   ChamadoRepository chamadoRepository,
                                   TecnicoRepository tecnicoRepository) {
        this.historicoRepository = historicoRepository;
        this.chamadoRepository = chamadoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public HistoricoChamadoResponseDTO create(HistoricoChamadoRequestDTO dto) {
        logger.info("Criando histórico para o chamado ID: {}", dto.chamadoId());

        // 1. Otimização com Proxy para o Chamado (obrigatório)
        Chamado chamado = chamadoRepository.getReferenceById(dto.chamadoId());

        // 2. Busca do Técnico (opcional)
        Tecnico tecnico = null;
        if (dto.tecnicoId() != null) {
            tecnico = tecnicoRepository.getReferenceById(dto.tecnicoId());
        }

        // 3. Mapeamento para Entidade
        HistoricoChamado historico = new HistoricoChamado();
        historico.setChamado(chamado);
        historico.setTecnico(tecnico);
        historico.setDescricao(dto.descricao());
        historico.setDataRegistro(LocalDateTime.now());

        HistoricoChamado historicoSalvo = historicoRepository.save(historico);
        logger.info("Histórico criado com sucesso. ID: {}", historicoSalvo.getId());

        return new HistoricoChamadoResponseDTO(historicoSalvo);
    }

    public HistoricoChamadoResponseDTO findById(Long id) {
        logger.info("Buscando histórico por ID: {}", id);
        if (id == null) {
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        }
        HistoricoChamado historico = historicoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        return new HistoricoChamadoResponseDTO(historico);
    }

    public Page<HistoricoChamadoResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todos os históricos com paginação");
        Page<HistoricoChamado> historicos = historicoRepository.findAll(pageable);
        return historicos.map(HistoricoChamadoResponseDTO::new);
    }

    public Page<HistoricoChamadoResponseDTO> findByChamado(Long chamadoId, Pageable pageable) {
        Page<HistoricoChamado> historicoChamados = historicoRepository.findByChamadoIdOrderByDataRegistroAsc(chamadoId, pageable);
        return historicoChamados.map(HistoricoChamadoResponseDTO::new);
    }

    public HistoricoChamadoResponseDTO update(Long id, HistoricoChamadoRequestDTO dto) {
        logger.info("Atualizando histórico ID: {}", id);

        HistoricoChamado updateHistorico = historicoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        // Atualização dos relacionamentos caso informados no DTO
        if (dto.chamadoId() != null) {
            Chamado novoChamado = chamadoRepository.getReferenceById(dto.chamadoId());
            updateHistorico.setChamado(novoChamado);
        }

        if (dto.tecnicoId() != null) {
            Tecnico novoTecnico = tecnicoRepository.getReferenceById(dto.tecnicoId());
            updateHistorico.setTecnico(novoTecnico);
        } else {
            updateHistorico.setTecnico(null);
        }

        updateHistorico.setDescricao(dto.descricao());

        HistoricoChamado historicoSalvo = historicoRepository.save(updateHistorico);
        logger.info("Histórico ID {} atualizado com sucesso", id);

        return new HistoricoChamadoResponseDTO(historicoSalvo);
    }

    public void delete(Long id) {
        logger.info("Deletando histórico ID: {}", id);

        HistoricoChamado historico = historicoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        historicoRepository.delete(historico);
        logger.info("Histórico deletado com sucesso");
    }
}