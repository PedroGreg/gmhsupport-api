package br.com.gmhsupport.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @JoinColumn(name = "id_subcategoria")
    private SubcategoriaProblema subcategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private StatusChamado status;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 4000)
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

    // ----- Local do atendimento -----
    @Column(name = "endereco_atendimento", length = 255)
    private String enderecoAtendimento;

    @Column(length = 120)
    private String complemento;

    private Double latitude;

    private Double longitude;

    @Column(name = "distancia_km", precision = 6, scale = 2)
    private BigDecimal distanciaKm;

    // ----- Código de confirmação para finalizar -----
    @Column(name = "codigo_confirmacao", length = 6)
    private String codigoConfirmacao;

    @Column(name = "codigo_expira_em")
    private LocalDateTime codigoExpiraEm;

    @Column(name = "codigo_tentativas", nullable = false)
    private Integer codigoTentativas = 0;

    // Solução escrita pelo técnico, guardada até o código ser confirmado
    @Column(length = 4000)
    private String solucao;

    // Lado "inverso": quem tem a coluna id_chamado são as tabelas pagamentos e adicionais
    @OneToMany(mappedBy = "chamado", fetch = FetchType.LAZY)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToOne(mappedBy = "chamado", fetch = FetchType.LAZY)
    private Adicional adicional;

    // Pagamento feito na abertura
    public Pagamento getPagamentoPrincipal() {
        return pagamentoDoTipo(TipoPagamento.principal);
    }

    // Pagamento do valor adicional (se houver)
    public Pagamento getPagamentoAdicional() {
        return pagamentoDoTipo(TipoPagamento.adicional);
    }

    private Pagamento pagamentoDoTipo(TipoPagamento tipo) {
        if (pagamentos == null) return null;
        return pagamentos.stream().filter(p -> p.getTipo() == tipo).findFirst().orElse(null);
    }

    public List<Pagamento> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<Pagamento> pagamentos) { this.pagamentos = pagamentos; }

    public Adicional getAdicional() { return adicional; }
    public void setAdicional(Adicional adicional) { this.adicional = adicional; }

    public SubcategoriaProblema getSubcategoria() { return subcategoria; }
    public void setSubcategoria(SubcategoriaProblema subcategoria) { this.subcategoria = subcategoria; }

    public String getEnderecoAtendimento() { return enderecoAtendimento; }
    public void setEnderecoAtendimento(String enderecoAtendimento) { this.enderecoAtendimento = enderecoAtendimento; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public BigDecimal getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(BigDecimal distanciaKm) { this.distanciaKm = distanciaKm; }

    public String getCodigoConfirmacao() { return codigoConfirmacao; }
    public void setCodigoConfirmacao(String codigoConfirmacao) { this.codigoConfirmacao = codigoConfirmacao; }

    public LocalDateTime getCodigoExpiraEm() { return codigoExpiraEm; }
    public void setCodigoExpiraEm(LocalDateTime codigoExpiraEm) { this.codigoExpiraEm = codigoExpiraEm; }

    public Integer getCodigoTentativas() { return codigoTentativas; }
    public void setCodigoTentativas(Integer codigoTentativas) { this.codigoTentativas = codigoTentativas; }

    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }

    // Há um código válido esperando o técnico digitar?
    public boolean isCodigoAtivo() {
        return codigoConfirmacao != null && codigoExpiraEm != null && codigoExpiraEm.isAfter(LocalDateTime.now());
    }

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
