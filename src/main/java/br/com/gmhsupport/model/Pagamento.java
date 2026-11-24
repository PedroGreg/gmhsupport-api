package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false)
    private Chamado chamado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPagamento tipo = TipoPagamento.principal;

    // Composição do valor (só no principal): serviço com acréscimo da prioridade + deslocamento
    @Column(name = "valor_servico", precision = 10, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "valor_deslocamento", precision = 10, scale = 2)
    private BigDecimal valorDeslocamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.pendente;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FormaPagamento forma;

    private Integer parcelas;

    // Só os 4 últimos dígitos: o número completo do cartão nunca é salvo
    @Column(name = "cartao_final", length = 4)
    private String cartaoFinal;

    @Column(name = "codigo_pix", length = 1000)
    private String codigoPix;

    @Column(length = 40)
    private String bandeira;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    // ----- Mercado Pago -----
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MeioPagamento meio;

    // Id do pagamento no Mercado Pago (Pix: criado na hora; cartão: chega na aprovação)
    @Column(name = "mp_pagamento_id", length = 40)
    private String mpPagamentoId;

    // Checkout Pro: id da "preferência" e o link da página de pagamento
    @Column(name = "mp_preferencia_id", length = 100)
    private String mpPreferenciaId;

    @Column(name = "link_pagamento", length = 500)
    private String linkPagamento;

    @Column(name = "pix_expira_em")
    private LocalDateTime pixExpiraEm;

    // Último status visto no Mercado Pago (approved, pending, rejected...) e o motivo
    @Column(name = "mp_status", length = 40)
    private String mpStatus;

    @Column(name = "mp_status_detalhe", length = 100)
    private String mpStatusDetalhe;

    // ----- Repasse ao técnico (o admin faz o Pix) -----
    @Enumerated(EnumType.STRING)
    @Column(name = "repasse_status", nullable = false, length = 20)
    private StatusRepasse repasseStatus = StatusRepasse.nao_liberado;

    @Column(name = "valor_repasse", precision = 10, scale = 2)
    private BigDecimal valorRepasse;

    @Column(name = "data_repasse_liberado")
    private LocalDateTime dataRepasseLiberado;

    @Column(name = "data_repasse_pago")
    private LocalDateTime dataRepassePago;

    @Column(name = "observacao_repasse", length = 255)
    private String observacaoRepasse;

    public Pagamento() {
    }

    public Pagamento(Chamado chamado, BigDecimal valor) {
        this.chamado = chamado;
        this.valor = valor;
        this.status = StatusPagamento.pendente;
    }

    public TipoPagamento getTipo() { return tipo; }
    public void setTipo(TipoPagamento tipo) { this.tipo = tipo; }

    public BigDecimal getValorServico() { return valorServico; }
    public void setValorServico(BigDecimal valorServico) { this.valorServico = valorServico; }

    public BigDecimal getValorDeslocamento() { return valorDeslocamento; }
    public void setValorDeslocamento(BigDecimal valorDeslocamento) { this.valorDeslocamento = valorDeslocamento; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public FormaPagamento getForma() { return forma; }
    public void setForma(FormaPagamento forma) { this.forma = forma; }

    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }

    public String getCartaoFinal() { return cartaoFinal; }
    public void setCartaoFinal(String cartaoFinal) { this.cartaoFinal = cartaoFinal; }

    public String getCodigoPix() { return codigoPix; }
    public void setCodigoPix(String codigoPix) { this.codigoPix = codigoPix; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }

    public StatusRepasse getRepasseStatus() { return repasseStatus; }
    public void setRepasseStatus(StatusRepasse repasseStatus) { this.repasseStatus = repasseStatus; }

    public BigDecimal getValorRepasse() { return valorRepasse; }
    public void setValorRepasse(BigDecimal valorRepasse) { this.valorRepasse = valorRepasse; }

    public LocalDateTime getDataRepasseLiberado() { return dataRepasseLiberado; }
    public void setDataRepasseLiberado(LocalDateTime dataRepasseLiberado) { this.dataRepasseLiberado = dataRepasseLiberado; }

    public LocalDateTime getDataRepassePago() { return dataRepassePago; }
    public void setDataRepassePago(LocalDateTime dataRepassePago) { this.dataRepassePago = dataRepassePago; }

    public String getObservacaoRepasse() { return observacaoRepasse; }
    public void setObservacaoRepasse(String observacaoRepasse) { this.observacaoRepasse = observacaoRepasse; }

    public MeioPagamento getMeio() { return meio; }
    public void setMeio(MeioPagamento meio) { this.meio = meio; }

    public String getMpPagamentoId() { return mpPagamentoId; }
    public void setMpPagamentoId(String mpPagamentoId) { this.mpPagamentoId = mpPagamentoId; }

    public String getMpPreferenciaId() { return mpPreferenciaId; }
    public void setMpPreferenciaId(String mpPreferenciaId) { this.mpPreferenciaId = mpPreferenciaId; }

    public String getLinkPagamento() { return linkPagamento; }
    public void setLinkPagamento(String linkPagamento) { this.linkPagamento = linkPagamento; }

    public LocalDateTime getPixExpiraEm() { return pixExpiraEm; }
    public void setPixExpiraEm(LocalDateTime pixExpiraEm) { this.pixExpiraEm = pixExpiraEm; }

    public String getMpStatus() { return mpStatus; }
    public void setMpStatus(String mpStatus) { this.mpStatus = mpStatus; }

    public String getMpStatusDetalhe() { return mpStatusDetalhe; }
    public void setMpStatusDetalhe(String mpStatusDetalhe) { this.mpStatusDetalhe = mpStatusDetalhe; }
}
