package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Valor a mais pedido pelo técnico quando o serviço é mais complexo ou diferente do solicitado.
// No máximo 1 por chamado.
@Entity
@Table(name = "adicionais")
public class Adicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adicional")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false, unique = true)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Tecnico tecnico;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAdicional status = StatusAdicional.pendente;

    @Enumerated(EnumType.STRING)
    @Column(name = "decisao_tecnico", length = 20)
    private DecisaoAdicional decisaoTecnico;

    @Column(name = "data_solicitacao", insertable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    public Adicional() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }

    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public StatusAdicional getStatus() { return status; }
    public void setStatus(StatusAdicional status) { this.status = status; }

    public DecisaoAdicional getDecisaoTecnico() { return decisaoTecnico; }
    public void setDecisaoTecnico(DecisaoAdicional decisaoTecnico) { this.decisaoTecnico = decisaoTecnico; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
}
