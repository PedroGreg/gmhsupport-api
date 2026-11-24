package br.com.gmhsupport.config;

import br.com.gmhsupport.model.Administrador;
import br.com.gmhsupport.repository.AdministradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Cria o primeiro administrador ao iniciar a API, a partir das variáveis de ambiente do Render:
//   ADMIN_EMAIL, ADMIN_PASSWORD (mín. 8 caracteres) e, opcional, ADMIN_NOME.
// A senha nunca fica no código. Se o admin já existe, nada muda (trocar ADMIN_PASSWORD depois não altera a senha).
// ADMIN_RESET_2FA=true: apaga o Authenticator do admin (celular perdido). Use uma vez e volte para false.
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInicial.class);

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gmh.admin.email:}")
    private String email;
    @Value("${gmh.admin.senha:}")
    private String senha;
    @Value("${gmh.admin.nome:Administrador}")
    private String nome;
    @Value("${gmh.admin.reset-2fa:false}")
    private boolean reset2fa;

    public AdminInicial(AdministradorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank()) {
            logger.info("ADMIN_EMAIL não definido: nenhum administrador criado automaticamente.");
            return;
        }
        String emailLimpo = email.trim().toLowerCase();
        var existente = repository.findByEmailIgnoreCase(emailLimpo);
        if (existente.isPresent()) {
            if (reset2fa) {
                Administrador admin = existente.get();
                admin.setTotpAtivo(false);
                admin.setTotpSegredo(null);
                admin.setUltimoPassoTotp(null);
                admin.setTentativasFalhas(0);
                admin.setBloqueadoAte(null);
                repository.save(admin);
                logger.warn("Authenticator do admin {} reiniciado (ADMIN_RESET_2FA=true). Volte a variável para false.", emailLimpo);
            }
            return;
        }
        if (senha == null || senha.length() < 8) {
            logger.warn("ADMIN_PASSWORD ausente ou com menos de 8 caracteres: administrador não criado.");
            return;
        }
        Administrador admin = new Administrador();
        admin.setNome(nome);
        admin.setEmail(emailLimpo);
        admin.setSenha(passwordEncoder.encode(senha));
        repository.save(admin);
        logger.info("Administrador {} criado. No primeiro login o app mostra como ativar o Authenticator.", emailLimpo);
    }
}
