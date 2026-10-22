package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.TecnicoRequestDTO;
import br.com.gmhsupport.dto.response.TecnicoEstatisticasResponseDTO;
import br.com.gmhsupport.dto.response.TecnicoResponseDTO;
import br.com.gmhsupport.dto.response.UsuarioResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Chamado;
import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.repository.ChamadoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ChamadoRepository chamadoRepository;

    Logger logger = LoggerFactory.getLogger(TecnicoService.class);
    public TecnicoService(TecnicoRepository repository, PasswordEncoder passwordEncoder, ChamadoRepository chamadoRepository){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.chamadoRepository = chamadoRepository;
    }

    public TecnicoResponseDTO create(TecnicoRequestDTO dto){
        logger.info("Criando tecnico");
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(dto.nome());
        tecnico.setEmail(dto.email().toLowerCase().trim());
        tecnico.setSenha(passwordEncoder.encode(dto.senha()));
        tecnico.setEspecialidade(dto.especialidade());
        tecnico.setDataCadastro(LocalDateTime.now());

        Tecnico createTecnico = repository.save(tecnico);
        logger.info("O id do tecnico é: " + createTecnico.getId());
        return new TecnicoResponseDTO(createTecnico);
    }

    public TecnicoResponseDTO findByEmailExato(String email) {
        return repository.findByEmailIgnoreCase(email).map(TecnicoResponseDTO::new)
                .orElseThrow(() -> new CustomNotFoundException("Tecnico não encontrado"));
    }

    @Transactional(readOnly = true)
    public TecnicoEstatisticasResponseDTO obterEstatisticas(String email) {
        logger.info("Calculando estatísticas para o técnico e-mail: {}", email);

        Tecnico tecnico = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado com o e-mail: " + email));

        // 1. Busca os chamados finalizados (Status ID 4L)
        List<Chamado> chamadosFinalizados = chamadoRepository.findFinalizadosByTecnicoId(tecnico.getId());
        long totalFinalizados = chamadosFinalizados.size();

        // 2. Busca a nota média das avaliações do técnico
        Double notaMediaRaw = chamadoRepository.findNotaMediaByTecnicoId(tecnico.getId());
        double notaMedia = (notaMediaRaw != null) ? Math.round(notaMediaRaw * 10.0) / 10.0 : 0.0;

        // 3. Calcula o tempo médio de atendimento (dataFechamento - dataAtendimento)
        long tempoMedioMinutos = 0L;
        if (!chamadosFinalizados.isEmpty()) {
            long totalMinutos = 0L;
            long chamadosComTempoValido = 0L;

            for (Chamado c : chamadosFinalizados) {
                if (c.getDataAtendimento() != null && c.getDataFechamento() != null) {
                    Duration duracao = Duration.between(c.getDataAtendimento(), c.getDataFechamento());
                    totalMinutos += duracao.toMinutes();
                    chamadosComTempoValido++;
                }
            }

            if (chamadosComTempoValido > 0) {
                tempoMedioMinutos = totalMinutos / chamadosComTempoValido;
            }
        }

        logger.info("Estatísticas geradas: Total finalizados: {}, Nota Média: {}, Tempo Médio: {} min",
                totalFinalizados, notaMedia, tempoMedioMinutos);

        return new TecnicoEstatisticasResponseDTO(totalFinalizados, notaMedia, tempoMedioMinutos);
    }

    public TecnicoResponseDTO findById(Long id) {
        logger.info("Buscando tecnico por id");
        if(id == null)
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        Tecnico findTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        logger.info("O id do tecnico é: " + id);
        return new TecnicoResponseDTO(findTecnico);
    }

    public Page<TecnicoResponseDTO> findAll(Pageable pageable){
        logger.info("Buscando todos tecnicos");
        Page<Tecnico> findTecnico = repository.findAll(pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public Page<TecnicoResponseDTO> findByNome(String nome, Pageable pageable){
        logger.info("Buscando todos tecnicos com o nome informado");
        Page<Tecnico> findTecnico = repository.findByNomeContainingIgnoreCase(nome, pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public Page<TecnicoResponseDTO> findByEmail(String email, Pageable pageable){
        logger.info("Buscando todos tecnicos com o email informado");
        Page<Tecnico> findTecnico = repository.findByEmailContainingIgnoreCase(email, pageable);
        return findTecnico.map(TecnicoResponseDTO::new);
    }

    public TecnicoResponseDTO update(TecnicoRequestDTO dto, Long id){
        logger.info("Atualizando tecnico");
        Tecnico updateTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        updateTecnico.setNome(dto.nome());
        updateTecnico.setEmail(dto.email().toLowerCase().trim());
        updateTecnico.setSenha(passwordEncoder.encode(dto.senha()));
        updateTecnico.setEspecialidade(dto.especialidade());

        updateTecnico = repository.save(updateTecnico);
        logger.info("O id do tecnico é: " + id);
        return new TecnicoResponseDTO(updateTecnico);
    }

    public void delete(Long id){
        logger.info("Deletando tecnico");
        Tecnico findTecnico = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        repository.delete(findTecnico);
        logger.info("Tecnico deletado");
    }
}