package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.TecnicoCadastroRequestDTO;
import br.com.gmhsupport.dto.response.TecnicoResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.DocumentoTecnico;
import br.com.gmhsupport.model.StatusCadastro;
import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.repository.AdministradorRepository;
import br.com.gmhsupport.repository.DocumentoTecnicoRepository;
import br.com.gmhsupport.repository.TecnicoRepository;
import br.com.gmhsupport.repository.UsuarioRepository;
import br.com.gmhsupport.util.Cpf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;

// "Quero ser técnico": cadastro público com CPF válido e certidão de antecedentes criminais.
// A conta nasce PENDENTE: entra no app, mas só vê "Cadastro em análise" até o admin aprovar.
@Service
public class TecnicoCadastroService {

    private static final Logger logger = LoggerFactory.getLogger(TecnicoCadastroService.class);
    public static final long TAMANHO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final DocumentoTecnicoRepository documentoRepository;
    private final PasswordEncoder passwordEncoder;

    public TecnicoCadastroService(TecnicoRepository tecnicoRepository, UsuarioRepository usuarioRepository,
                                  AdministradorRepository administradorRepository,
                                  DocumentoTecnicoRepository documentoRepository, PasswordEncoder passwordEncoder) {
        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
        this.documentoRepository = documentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TecnicoResponseDTO cadastrar(TecnicoCadastroRequestDTO dto, MultipartFile antecedentes) {
        if (dto == null) throw new IllegalArgumentException("Envie os dados do cadastro.");

        String nome = texto(dto.nome());
        String email = texto(dto.email()).toLowerCase();
        String senha = dto.senha() == null ? "" : dto.senha();
        String cpf = Cpf.somenteNumeros(dto.cpf());
        String telefone = texto(dto.telefone());
        String chavePix = texto(dto.chavePix());
        List<String> especialidades = dto.especialidades() == null ? List.of() :
                dto.especialidades().stream().filter(Objects::nonNull).map(String::trim)
                        .filter(s -> !s.isEmpty()).distinct().toList();

        // ----- Regras (mensagens em português, mostradas direto no app) -----
        if (nome.length() < 3 || nome.length() > 150) throw new IllegalArgumentException("Informe o nome completo (3 a 150 letras).");
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || email.length() > 150) throw new IllegalArgumentException("E-mail inválido.");
        if (senha.length() < 6 || senha.length() > 100) throw new IllegalArgumentException("A senha deve ter entre 6 e 100 caracteres.");
        if (!Cpf.valido(cpf)) throw new IllegalArgumentException("CPF inválido. Confira os números.");
        if (telefone.replaceAll("\\D", "").length() < 10) throw new IllegalArgumentException("Informe um celular com DDD.");
        if (dto.dataNascimento() == null) throw new IllegalArgumentException("Informe a data de nascimento.");
        int idade = Period.between(dto.dataNascimento(), LocalDate.now()).getYears();
        if (idade < 18) throw new IllegalArgumentException("É preciso ter 18 anos ou mais para ser técnico.");
        if (idade > 100) throw new IllegalArgumentException("Data de nascimento inválida.");
        if (especialidades.isEmpty()) throw new IllegalArgumentException("Escolha pelo menos uma especialidade.");
        String especialidade = String.join(", ", especialidades);
        if (especialidade.length() > 500) throw new IllegalArgumentException("Especialidades demais.");
        if (chavePix.length() < 5 || chavePix.length() > 150) throw new IllegalArgumentException("Informe a chave Pix para receber os pagamentos.");
        if (!Boolean.TRUE.equals(dto.aceitouTermos())) throw new IllegalArgumentException("Aceite os termos e a política de privacidade para continuar.");

        if (tecnicoRepository.findByEmailIgnoreCase(email).isPresent()
                || usuarioRepository.findByEmailIgnoreCase(email).isPresent()
                || administradorRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalStateException("Este e-mail já está cadastrado.");
        }
        if (tecnicoRepository.existsByCpf(cpf)) throw new IllegalStateException("Este CPF já está cadastrado.");

        ArquivoValidado arquivo = validarArquivo(antecedentes);

        Tecnico tecnico = new Tecnico();
        tecnico.setNome(nome);
        tecnico.setEmail(email);
        tecnico.setSenha(passwordEncoder.encode(senha));
        tecnico.setCpf(cpf);
        tecnico.setTelefone(telefone);
        tecnico.setDataNascimento(dto.dataNascimento());
        tecnico.setEspecialidade(especialidade);
        tecnico.setChavePix(chavePix);
        tecnico.setDisponivel(true);
        tecnico.setStatusCadastro(StatusCadastro.pendente);
        tecnico.setTermosAceitosEm(LocalDateTime.now());
        tecnico.setDataCadastro(LocalDateTime.now());
        Tecnico salvo = tecnicoRepository.save(tecnico);

        salvarDocumento(salvo, arquivo);
        logger.info("Novo técnico {} cadastrado (pendente de análise)", salvo.getId());
        return new TecnicoResponseDTO(salvo);
    }

    // Recusado: o técnico manda uma nova certidão e volta para a fila de análise
    @Transactional
    public TecnicoResponseDTO reenviarDocumento(String email, MultipartFile antecedentes) {
        Tecnico tecnico = tecnicoRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomNotFoundException("Técnico não encontrado"));
        if (tecnico.getStatusCadastro() == StatusCadastro.aprovado) {
            throw new IllegalStateException("Seu cadastro já está aprovado.");
        }
        ArquivoValidado arquivo = validarArquivo(antecedentes);
        salvarDocumento(tecnico, arquivo);
        tecnico.setStatusCadastro(StatusCadastro.pendente);
        tecnico.setMotivoRecusa(null);
        tecnico.setDataAnalise(null);
        logger.info("Técnico {} reenviou a certidão", tecnico.getId());
        return new TecnicoResponseDTO(tecnicoRepository.save(tecnico));
    }

    private void salvarDocumento(Tecnico tecnico, ArquivoValidado arquivo) {
        DocumentoTecnico doc = documentoRepository.findByTecnicoId(tecnico.getId()).orElseGet(DocumentoTecnico::new);
        doc.setTecnico(tecnico);
        doc.setNomeArquivo(arquivo.nome());
        doc.setTipoConteudo(arquivo.tipo());
        doc.setTamanho((long) arquivo.bytes().length);
        doc.setConteudo(arquivo.bytes());
        doc.setDataEnvio(LocalDateTime.now());
        documentoRepository.save(doc);
    }

    private record ArquivoValidado(String nome, String tipo, byte[] bytes) {}

    // PDF, JPG ou PNG de até 5 MB. O tipo é conferido pelos primeiros bytes, não pelo nome do arquivo.
    private ArquivoValidado validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Anexe a certidão de antecedentes criminais (PDF, JPG ou PNG).");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O arquivo deve ter no máximo 5 MB.");
        }
        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo enviado.");
        }
        String tipo;
        String extensao;
        if (comeca(bytes, 0x25, 0x50, 0x44, 0x46)) { tipo = "application/pdf"; extensao = ".pdf"; }       // %PDF
        else if (comeca(bytes, 0xFF, 0xD8, 0xFF)) { tipo = "image/jpeg"; extensao = ".jpg"; }
        else if (comeca(bytes, 0x89, 0x50, 0x4E, 0x47)) { tipo = "image/png"; extensao = ".png"; }
        else throw new IllegalArgumentException("Formato não aceito. Envie a certidão em PDF, JPG ou PNG.");

        String nome = arquivo.getOriginalFilename();
        if (nome == null || nome.isBlank()) nome = "antecedentes" + extensao;
        nome = nome.replaceAll("[^A-Za-z0-9._ -]", "_");
        if (nome.length() > 120) nome = nome.substring(nome.length() - 120);
        return new ArquivoValidado(nome, tipo, bytes);
    }

    private boolean comeca(byte[] b, int... assinatura) {
        if (b.length < assinatura.length) return false;
        for (int i = 0; i < assinatura.length; i++) {
            if ((b[i] & 0xFF) != assinatura[i]) return false;
        }
        return true;
    }

    private String texto(String s) {
        return s == null ? "" : s.trim();
    }
}
