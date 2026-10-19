package br.com.gmhsupport.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chamado")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private CategoriaProblema categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private StatusChamado status;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Prioridade prioridade = Prioridade.media;

    @Column(name = "data_abertura", insertable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_atendimento")
    private LocalDateTime dataAtendimento;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    public Chamado() {
    }

    public Chamado(Usuario usuario, Tecnico tecnico, CategoriaProblema categoria, StatusChamado status, String titulo, String descricao, Prioridade prioridade, LocalDateTime dataAbertura, LocalDateTime dataAtendimento, LocalDateTime dataFechamento) {
        this.usuario = usuario;
        this.tecnico = tecnico;
        this.categoria = categoria;
        this.status = status;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.dataAbertura = dataAbertura;
        this.dataAtendimento = dataAtendimento;
        this.dataFechamento = dataFechamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public CategoriaProblema getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProblema categoria) {
        this.categoria = categoria;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataAtendimento() {
        return dataAtendimento;
    }

    public void setDataAtendimento(LocalDateTime dataAtendimento) {
        this.dataAtendimento = dataAtendimento;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chamado chamado = (Chamado) o;
        return Objects.equals(id, chamado.id) && Objects.equals(usuario, chamado.usuario) && Objects.equals(tecnico, chamado.tecnico) && Objects.equals(categoria, chamado.categoria) && Objects.equals(status, chamado.status) && Objects.equals(titulo, chamado.titulo) && Objects.equals(descricao, chamado.descricao) && prioridade == chamado.prioridade && Objects.equals(dataAbertura, chamado.dataAbertura) && Objects.equals(dataAtendimento, chamado.dataAtendimento) && Objects.equals(dataFechamento, chamado.dataFechamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuario, tecnico, categoria, status, titulo, descricao, prioridade, dataAbertura, dataAtendimento, dataFechamento);
    }
}
