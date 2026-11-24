package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.FormaPagamento;
import br.com.gmhsupport.model.Pagamento;
import br.com.gmhsupport.model.StatusPagamento;
import br.com.gmhsupport.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long id,
        Long chamadoId,
        TipoPagamento tipo,
        BigDecimal valor,
        BigDecimal valorServico,
        BigDecimal valorDeslocamento,
        StatusPagamento status,
        FormaPagamento forma,
        Integer parcelas,
        String cartaoFinal,
        String bandeira,
        String codigoPix,
        LocalDateTime dataCriacao,
        LocalDateTime dataPagamento
) {
    public PagamentoResponseDTO(Pagamento pagamento) {
        this(
                pagamento.getId(),
                pagamento.getChamado() != null ? pagamento.getChamado().getId() : null,
                pagamento.getTipo(),
                pagamento.getValor(),
                pagamento.getValorServico(),
                pagamento.getValorDeslocamento(),
                pagamento.getStatus(),
                pagamento.getForma(),
                pagamento.getParcelas(),
                pagamento.getCartaoFinal(),
                pagamento.getBandeira(),
                pagamento.getCodigoPix(),
                pagamento.getDataCriacao(),
                pagamento.getDataPagamento()
        );
    }
}
