package br.com.gmhsupport.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false, unique = true)
    private Chamado chamado;

    @Column(nullable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "data_avaliacao", insertable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {
    }

    public Avaliacao(Chamado chamado, Integer nota, String comentario, LocalDateTime dataAvaliacao) {
        this.chamado = chamado;
        this.nota = nota;
        this.comentario = comentario;
        this.dataAvaliacao = dataAvaliacao;
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

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Avaliacao avaliacao = (Avaliacao) o;
        return Objects.equals(id, avaliacao.id) && Objects.equals(chamado, avaliacao.chamado) && Objects.equals(nota, avaliacao.nota) && Objects.equals(comentario, avaliacao.comentario) && Objects.equals(dataAvaliacao, avaliacao.dataAvaliacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chamado, nota, comentario, dataAvaliacao);
    }
}
