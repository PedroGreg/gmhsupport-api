package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Cartão salvo do usuário ("Meus cartões").
// Por segurança só guardamos o que aparece na tela: bandeira, 4 últimos dígitos, nome e validade.
// O número completo e o CVV nunca chegam na API.
@Entity
@Table(name = "cartoes")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cartao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(length = 40)
    private String apelido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCartao tipo;

    @Column(nullable = false, length = 20)
    private String bandeira;

    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(name = "nome_titular", nullable = false, length = 100)
    private String nomeTitular;

    @Column(name = "mes_validade", nullable = false)
    private Integer mesValidade;

    @Column(name = "ano_validade", nullable = false)
    private Integer anoValidade;

    @Column(nullable = false)
    private Boolean principal = false;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Cartao() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }

    public TipoCartao getTipo() { return tipo; }
    public void setTipo(TipoCartao tipo) { this.tipo = tipo; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public String getUltimosDigitos() { return ultimosDigitos; }
    public void setUltimosDigitos(String ultimosDigitos) { this.ultimosDigitos = ultimosDigitos; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public Integer getMesValidade() { return mesValidade; }
    public void setMesValidade(Integer mesValidade) { this.mesValidade = mesValidade; }

    public Integer getAnoValidade() { return anoValidade; }
    public void setAnoValidade(Integer anoValidade) { this.anoValidade = anoValidade; }

    public Boolean getPrincipal() { return principal; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
