package br.com.gmhsupport.services;

import br.com.gmhsupport.dto.request.AdminLoginRequestDTO;
import br.com.gmhsupport.dto.request.AdminVerificarRequestDTO;
import br.com.gmhsupport.dto.response.AdminLoginResponseDTO;
import br.com.gmhsupport.dto.response.TokenResponseDTO;
import br.com.gmhsupport.model.Administrador;
import br.com.gmhsupport.repository.AdministradorRepository;
import br.com.gmhsupport.security.TokenService;
import br.com.gmhsupport.security.TotpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Login do administrador em 2 etapas:
//  1) POST /admin/login {email, senha}            -> token temporário (5 min). No 1º acesso, também o segredo/QR.
//  2) POST /admin/login/verificar {token, codigo} -> token de 8 h com ROLE_ADMIN
// 5 erros seguidos (senha ou código) bloqueiam a conta por 15 minutos.
@Service
public class AdminAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);
    private static final int MAX_FALHAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TotpService totpService;

    public AdminAuthService(AdministradorRepository repository, PasswordEncoder passwordEncoder,
                            TokenService tokenService, TotpService totpService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.totpService = totpService;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AdminLoginResponseDTO etapaSenha(AdminLoginRequestDTO dto) {
        Administrador admin = repository.findByEmailIgnoreCase(dto.email().trim()).orElse(null);
        if (admin == null) {
            // Mesmo custo de tempo de uma senha errada (não revela se o e-mail existe)
            passwordEncoder.matches(dto.senha(), "$2a$10$5zpYsJI1T25DaV0OgDRm3uq6uZPW7cqktOOCAA62NYI67dVXHQatK");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail e/ou senha incorretos.");
        }
        verificarBloqueio(admin);
        if (!passwordEncoder.matches(dto.senha(), admin.getSenha())) {
            registrarFalha(admin);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail e/ou senha incorretos.");
        }

        String tokenTemporario = tokenService.gerarTokenTemporarioAdmin(admin.getEmail());
        if (Boolean.TRUE.equals(admin.getTotpAtivo())) {
            return new AdminLoginResponseDTO(tokenTemporario, false, null, null, TotpService.EMISSOR,
                    "Digite o código de 6 dígitos do Google Authenticator.");
        }

        // Primeiro acesso (ou 2FA reiniciado): gera o segredo e devolve para configurar o Authenticator
        if (admin.getTotpSegredo() == null) {
            admin.setTotpSegredo(totpService.novoSegredo());
            repository.save(admin);
        }
        return new AdminLoginResponseDTO(tokenTemporario, true, admin.getTotpSegredo(),
                totpService.urlOtpauth(admin.getTotpSegredo(), admin.getEmail()), TotpService.EMISSOR,
                "Adicione a conta no Google Authenticator e digite o código que aparecer.");
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public TokenResponseDTO etapaCodigo(AdminVerificarRequestDTO dto) {
        String email = tokenService.validarTokenTemporarioAdmin(dto.tokenTemporario());
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tempo para digitar o código acabou. Entre com a senha de novo.");
        }
        Administrador admin = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Administrador não encontrado."));
        verificarBloqueio(admin);

        long passo = totpService.verificar(admin.getTotpSegredo(), dto.codigo(), admin.getUltimoPassoTotp());
        if (passo < 0) {
            registrarFalha(admin);
            int restantes = Math.max(0, MAX_FALHAS - admin.getTentativasFalhas());
            String msg = admin.isBloqueado()
                    ? "Código incorreto. Conta bloqueada até " + admin.getBloqueadoAte().format(HORA) + "."
                    : "Código incorreto ou já usado. Restam " + restantes + " tentativas.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        boolean primeiraVez = !Boolean.TRUE.equals(admin.getTotpAtivo());
        admin.setTotpAtivo(true);
        admin.setUltimoPassoTotp(passo);
        admin.setTentativasFalhas(0);
        admin.setBloqueadoAte(null);
        repository.save(admin);
        logger.info("Admin {} entrou{}", admin.getEmail(), primeiraVez ? " (Authenticator ativado agora)" : "");

        return new TokenResponseDTO(tokenService.gerarToken(admin.getEmail(), "ROLE_ADMIN"), admin.getNome(), "ROLE_ADMIN");
    }

    private void verificarBloqueio(Administrador admin) {
        if (admin.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Muitas tentativas erradas. Tente de novo depois das " + admin.getBloqueadoAte().format(HORA) + ".");
        }
    }

    private void registrarFalha(Administrador admin) {
        int falhas = admin.getTentativasFalhas() + 1;
        admin.setTentativasFalhas(falhas);
        if (falhas >= MAX_FALHAS) {
            admin.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
            admin.setTentativasFalhas(0);
            logger.warn("Admin {} bloqueado por {} minutos", admin.getEmail(), MINUTOS_BLOQUEIO);
        }
        repository.save(admin);
    }
}
