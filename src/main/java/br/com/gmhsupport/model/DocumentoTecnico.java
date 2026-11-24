package br.com.gmhsupport.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Certidão de antecedentes criminais enviada no cadastro (PDF, JPG ou PNG, até 5 MB).
// Fica no banco (BYTEA) e só o administrador consegue baixar.
@Entity
@Table(name = "documentos_tecnico")
public class DocumentoTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico", nullable = false, unique = true)
    private Tecnico tecnico;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;

    @Column(nullable = false)
    private Long tamanho;

    // Sem @Lob: no PostgreSQL @Lob vira "oid"; byte[] puro mapeia direto para BYTEA
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] conteudo;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    public DocumentoTecnico() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }

    public Long getTamanho() { return tamanho; }
    public void setTamanho(Long tamanho) { this.tamanho = tamanho; }

    public byte[] getConteudo() { return conteudo; }
    public void setConteudo(byte[] conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
}
