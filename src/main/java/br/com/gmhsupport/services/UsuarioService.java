package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.UsuarioRequestDTO;
import br.com.gmhsupport.dto.response.UsuarioResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Usuario;
import br.com.gmhsupport.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        logger.info("Criando usuario");
        Usuario user = new Usuario();
        user.setNome(dto.nome());
        user.setEmail(dto.email().toLowerCase().trim());
        user.setSenha(passwordEncoder.encode(dto.senha()));
        user.setTelefone(dto.telefone());
        user.setEndereco(dto.endereco());
        user.setDataCadastro(LocalDateTime.now());

        Usuario createUser = repository.save(user);
        logger.info("O id do usuario é: " + createUser.getId());
        return new UsuarioResponseDTO(createUser);
    }

    public UsuarioResponseDTO findByEmailExato(String email) {
        return repository.findByEmailIgnoreCase(email).map(UsuarioResponseDTO::new)
                .orElseThrow(() -> new CustomNotFoundException("Usuário não encontrado"));
    }

    public UsuarioResponseDTO findById(Long id) {
        logger.info("Buscando usuario por id");
        if (id == null)
            throw new CustomNotFoundException("O ID deve ser preenchido e diferente de null");
        Usuario findUser = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        logger.info("O id do usuario é: " + id);
        return new UsuarioResponseDTO(findUser);
    }

    public Page<UsuarioResponseDTO> findAll(Pageable pageable) {
        logger.info("Buscando todos usuarios");
        Page<Usuario> findUser = repository.findAll(pageable);
        return findUser.map(UsuarioResponseDTO::new);
    }

    public Page<UsuarioResponseDTO> findByNome(String nome, Pageable pageable) {
        logger.info("Buscando todos usuario com o nome informado");
        Page<Usuario> findUser = repository.findByNomeContainingIgnoreCase(nome, pageable);
        return findUser.map(UsuarioResponseDTO::new);
    }

    public Page<UsuarioResponseDTO> findByEmail(String email, Pageable pageable) {
        logger.info("Buscando todos usuario com o email informado");
        Page<Usuario> findUser = repository.findByEmailContainingIgnoreCase(email, pageable);
        return findUser.map(UsuarioResponseDTO::new);
    }

    public UsuarioResponseDTO update(UsuarioRequestDTO dto, Long id) {
        logger.info("Atualizando usuario");
        Usuario updateUser = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        updateUser.setNome(dto.nome());
        updateUser.setEmail(dto.email().toLowerCase().trim());
        updateUser.setSenha(passwordEncoder.encode(dto.senha()));
        updateUser.setTelefone(dto.telefone());
        updateUser.setEndereco(dto.endereco());

        updateUser = repository.save(updateUser);
        logger.info("O id do usuario é: " + id);
        return new UsuarioResponseDTO(updateUser);
    }

    public void delete(Long id) {
        logger.info("Deletando usuario");
        Usuario findUser = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(id));
        repository.delete(findUser);
        logger.info("Usuario deletado");
    }
}