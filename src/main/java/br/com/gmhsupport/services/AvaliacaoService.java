package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.AvaliacaoRequestDTO;
import br.com.gmhsupport.dto.response.AvaliacaoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Avaliacao;
import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.repository.AvaliacaoRepository;
import br.com.gmhsupport.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ChamadoRepository chamadoRepository;

    private static final Logger logger = LoggerFactory.getLogger(AvaliacaoService.class);

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, ChamadoRepository chamadoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    public AvaliacaoResponseDTO create(AvaliacaoRequestDTO dto) {
        logger.info("Criando avaliação para o chamado ID: {}", dto.chamadoId());

        // 1. Otimização com Proxy para associar o Chamado sem SELECT desnecessário
        Chamado chamado = chamadoRepository.getReferenceById(dto.chamadoId());

        // 2. Instancia e mapeia a nova Avaliação
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setChamado(chamado);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);
        logger.info("Avaliação criada com sucesso. ID: {}", avaliacaoSalva.getId());

        return new AvaliacaoResponseDTO(avaliacaoSalva);
    }

    public AvaliacaoResponseDTO findById(Long id) {
        logger.info("Buscando avaliação por ID: {}", id);
        if (id == null) {
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        }
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        return new AvaliacaoResponseDTO(avaliacao);
    }

    public Page<AvaliacaoResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todas as avaliações com paginação");
        Page<Avaliacao> avaliacoes = avaliacaoRepository.findAll(pageable);
        return avaliacoes.map(AvaliacaoResponseDTO::new);
    }

    public AvaliacaoResponseDTO update(Long id, AvaliacaoRequestDTO dto) {
        logger.info("Atualizando avaliação ID: {}", id);

        Avaliacao updateAvaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));

        // Se o usuário alterar o chamado associado à avaliação
        if (dto.chamadoId() != null) {
            Chamado novoChamado = chamadoRepository.getReferenceById(dto.chamadoId());
            updateAvaliacao.setChamado(novoChamado);
        }

        updateAvaliacao.setNota(dto.nota());
        updateAvaliacao.setComentario(dto.comentario());

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(updateAvaliacao);
        logger.info("Avaliação ID {} atualizada com sucesso", id);

        return new AvaliacaoResponseDTO(avaliacaoSalva);
    }

    public void delete(Long id) {
        logger.info("Deletando avaliação ID: {}", id);
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(id));
        avaliacaoRepository.delete(avaliacao);
        logger.info("Avaliação deletada com sucesso");
    }
}