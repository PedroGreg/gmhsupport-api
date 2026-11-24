package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

// O problema em si, dentro de uma categoria (ex.: Hardware > "Notebook não liga"), com o preço base.
@Entity
@Table(name = "subcategorias_problema")
public class SubcategoriaProblema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subcategoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaProblema categoria;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Column(nullable = false)
    private Boolean ativo = true;

    public SubcategoriaProblema() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CategoriaProblema getCategoria() { return categoria; }
    public void setCategoria(CategoriaProblema categoria) { this.categoria = categoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
