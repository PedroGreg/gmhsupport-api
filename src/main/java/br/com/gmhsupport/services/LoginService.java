package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.LoginRequestDTO;
import br.com.gmhsupport.dto.response.TokenResponseDTO;
import br.com.gmhsupport.exception.CustomNotFoundException;
import br.com.gmhsupport.model.Tecnico;
import br.com.gmhsupport.model.Usuario;
import br.com.gmhsupport.repository.TecnicoRepository;
import br.com.gmhsupport.repository.UsuarioRepository;
import br.com.gmhsupport.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final TokenService service;
    private final PasswordEncoder passwordEncoder;


    public LoginService(UsuarioRepository usuarioRepository, TecnicoRepository tecnicoRepository, TokenService service, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.service = service;
        this.passwordEncoder = passwordEncoder;
    }

    private Logger logger = LoggerFactory.getLogger(LoginService.class);

    public ResponseEntity<?> onLogin(LoginRequestDTO request){
        logger.info("Logando usuário com e-mail: " + request.email());
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(request.email());
        if(usuarioOptional.isPresent()){
            Usuario usuario = usuarioOptional.get();
            if(passwordEncoder.matches(request.password(), usuario.getSenha())){
                logger.info("Login efetuado com sucesso para usuario {}", request.email());
                String token = service.gerarToken(request.email(), "ROLE_USUARIO");
                return new ResponseEntity<>(new TokenResponseDTO(token, usuario.getNome(), "ROLE_USUARIO"), HttpStatus.OK);
            }
        }
        Optional<Tecnico> tecnicoOptional = tecnicoRepository.findByEmailIgnoreCase(request.email());
        if(tecnicoOptional.isPresent()){
            Tecnico tecnico = tecnicoOptional.get();
            if(passwordEncoder.matches(request.password(), tecnico.getSenha())){
                logger.info("Login efetuado com sucesso para tecnico {}", request.email());
                String token = service.gerarToken(request.email(), "ROLE_TECNICO");
                return new ResponseEntity<>(new TokenResponseDTO(token, tecnico.getNome(), "ROLE_TECNICO"), HttpStatus.OK);
            }
        }
        logger.info("Email e/ou senha incorretos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("EMAIL E/OU SENHA INCORRETOS");
    }
}
