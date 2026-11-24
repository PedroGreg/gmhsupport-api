package br.com.gmhsupport.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tecnicos")
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tecnico")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(length = 500)
    private String especialidade;

    // Só números (11 dígitos), validado pelos dígitos verificadores
    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // Para onde o administrador faz o repasse (80% do valor)
    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_cadastro", nullable = false, length = 20)
    private StatusCadastro statusCadastro = StatusCadastro.pendente;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Column(name = "termos_aceitos_em")
    private LocalDateTime termosAceitosEm;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(nullable = false)
    private Boolean disponivel = true;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    public Tecnico() {
    }

    public Tecnico(LocalDateTime dataCadastro, Boolean disponivel, String especialidade, String senha, String email, String nome) {
        this.dataCadastro = dataCadastro;
        this.disponivel = disponivel;
        this.especialidade = especialidade;
        this.senha = senha;
        this.email = email;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Boolean getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAprovado() {
        return statusCadastro == StatusCadastro.aprovado;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public StatusCadastro getStatusCadastro() { return statusCadastro; }
    public void setStatusCadastro(StatusCadastro statusCadastro) { this.statusCadastro = statusCadastro; }

    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String motivoRecusa) { this.motivoRecusa = motivoRecusa; }

    public LocalDateTime getTermosAceitosEm() { return termosAceitosEm; }
    public void setTermosAceitosEm(LocalDateTime termosAceitosEm) { this.termosAceitosEm = termosAceitosEm; }

    public LocalDateTime getDataAnalise() { return dataAnalise; }
    public void setDataAnalise(LocalDateTime dataAnalise) { this.dataAnalise = dataAnalise; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tecnico tecnico = (Tecnico) o;
        return id != null && Objects.equals(id, tecnico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
