package br.com.gmhsupport.model;

import jakarta.persistence.*;
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

    @Column(length = 100)
    private String especialidade;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tecnico tecnico = (Tecnico) o;
        return Objects.equals(id, tecnico.id) && Objects.equals(nome, tecnico.nome) && Objects.equals(email, tecnico.email) && Objects.equals(senha, tecnico.senha) && Objects.equals(especialidade, tecnico.especialidade) && Objects.equals(disponivel, tecnico.disponivel) && Objects.equals(dataCadastro, tecnico.dataCadastro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, senha, especialidade, disponivel, dataCadastro);
    }
}
