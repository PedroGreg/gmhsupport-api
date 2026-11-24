package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Administrador: entra com e-mail + senha + código de 6 dígitos do Google Authenticator (TOTP).
// O primeiro admin é criado ao iniciar a API, a partir de ADMIN_EMAIL e ADMIN_PASSWORD (Render).
@Entity
@Table(name = "administradores")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    // Segredo em base32 que o Authenticator guarda. Nunca sai da API depois de ativado.
    @Column(name = "totp_segredo", length = 64)
    private String totpSegredo;

    // false = ainda não confirmou o primeiro código (o app mostra o QR/segredo para configurar)
    @Column(name = "totp_ativo", nullable = false)
    private Boolean totpAtivo = false;

    // Último "passo" de 30 s aceito: impede usar o mesmo código duas vezes
    @Column(name = "ultimo_passo_totp")
    private Long ultimoPassoTotp;

    @Column(name = "tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Administrador() {
    }

    public boolean isBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTotpSegredo() { return totpSegredo; }
    public void setTotpSegredo(String totpSegredo) { this.totpSegredo = totpSegredo; }

    public Boolean getTotpAtivo() { return totpAtivo; }
    public void setTotpAtivo(Boolean totpAtivo) { this.totpAtivo = totpAtivo; }

    public Long getUltimoPassoTotp() { return ultimoPassoTotp; }
    public void setUltimoPassoTotp(Long ultimoPassoTotp) { this.ultimoPassoTotp = ultimoPassoTotp; }

    public Integer getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(Integer tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }

    public LocalDateTime getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(LocalDateTime bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
}
