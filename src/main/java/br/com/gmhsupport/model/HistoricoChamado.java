package br.com.gmhsupport.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "historico_chamado")
public class HistoricoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_registro", insertable = false, updatable = false)
    private LocalDateTime dataRegistro;

    public HistoricoChamado() {
    }

    public HistoricoChamado(Chamado chamado, Tecnico tecnico, String descricao, LocalDateTime dataRegistro) {
        this.chamado = chamado;
        this.tecnico = tecnico;
        this.descricao = descricao;
        this.dataRegistro = dataRegistro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoChamado that = (HistoricoChamado) o;
        return Objects.equals(id, that.id) && Objects.equals(chamado, that.chamado) && Objects.equals(tecnico, that.tecnico) && Objects.equals(descricao, that.descricao) && Objects.equals(dataRegistro, that.dataRegistro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chamado, tecnico, descricao, dataRegistro);
    }
}