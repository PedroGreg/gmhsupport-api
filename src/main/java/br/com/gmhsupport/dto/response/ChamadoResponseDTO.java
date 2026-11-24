package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChamadoResponseDTO(
        Long id,
        Long usuarioId,
        String usuarioNome,
        String usuarioTelefone,
        String usuarioEndereco,
        Long tecnicoId,
        String tecnicoNome,
        Long categoriaId,
        String categoriaNome,
        Long subcategoriaId,
        String subcategoriaNome,
        Long statusId,
        String statusDescricao,
        String titulo,
        String descricao,
        Prioridade prioridade,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtendimento,
        LocalDateTime dataFechamento,

        // Local do atendimento
        String enderecoAtendimento,
        String complemento,
        Double latitude,
        Double longitude,
        BigDecimal distanciaKm,

        // Pagamento principal (feito na abertura)
        BigDecimal valor,
        BigDecimal valorServico,
        BigDecimal valorDeslocamento,
        StatusPagamento statusPagamento,
        FormaPagamento formaPagamento,

        // Adicional pedido pelo técnico (null se não houver)
        BigDecimal valorAdicional,
        String motivoAdicional,
        StatusAdicional statusAdicional,
        DecisaoAdicional decisaoAdicional,
        StatusPagamento statusPagamentoAdicional,

        // Código de confirmação: só diz se existe um válido (o número em si só o cliente vê)
        boolean codigoPendente,
        LocalDateTime codigoExpiraEm
) {
    public ChamadoResponseDTO(Chamado c) {
        this(
                c.getId(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getUsuario() != null ? c.getUsuario().getNome() : null,
                c.getUsuario() != null ? c.getUsuario().getTelefone() : null,
                c.getUsuario() != null ? c.getUsuario().getEndereco() : null,
                c.getTecnico() != null ? c.getTecnico().getId() : null,
                c.getTecnico() != null ? c.getTecnico().getNome() : null,
                c.getCategoria() != null ? c.getCategoria().getId() : null,
                c.getCategoria() != null ? c.getCategoria().getNome() : null,
                c.getSubcategoria() != null ? c.getSubcategoria().getId() : null,
                c.getSubcategoria() != null ? c.getSubcategoria().getNome() : null,
                c.getStatus() != null ? c.getStatus().getId() : null,
                c.getStatus() != null ? c.getStatus().getDescricao() : null,
                c.getTitulo(),
                c.getDescricao(),
                c.getPrioridade(),
                c.getDataAbertura(),
                c.getDataAtendimento(),
                c.getDataFechamento(),

                c.getEnderecoAtendimento(),
                c.getComplemento(),
                c.getLatitude(),
                c.getLongitude(),
                c.getDistanciaKm(),

                c.getPagamentoPrincipal() != null ? c.getPagamentoPrincipal().getValor() : null,
                c.getPagamentoPrincipal() != null ? c.getPagamentoPrincipal().getValorServico() : null,
                c.getPagamentoPrincipal() != null ? c.getPagamentoPrincipal().getValorDeslocamento() : null,
                c.getPagamentoPrincipal() != null ? c.getPagamentoPrincipal().getStatus() : null,
                c.getPagamentoPrincipal() != null ? c.getPagamentoPrincipal().getForma() : null,

                c.getAdicional() != null ? c.getAdicional().getValor() : null,
                c.getAdicional() != null ? c.getAdicional().getMotivo() : null,
                c.getAdicional() != null ? c.getAdicional().getStatus() : null,
                c.getAdicional() != null ? c.getAdicional().getDecisaoTecnico() : null,
                c.getPagamentoAdicional() != null ? c.getPagamentoAdicional().getStatus() : null,

                c.isCodigoAtivo(),
                c.isCodigoAtivo() ? c.getCodigoExpiraEm() : null
        );
    }
}
