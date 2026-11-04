package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.FormaPagamento;
import br.com.gmhsupport.model.Pagamento;
import br.com.gmhsupport.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long id,
        Long chamadoId,
        BigDecimal valor,
        StatusPagamento status,
        FormaPagamento forma,
        Integer parcelas,
        String cartaoFinal,
        String codigoPix,
        LocalDateTime dataCriacao,
        LocalDateTime dataPagamento
) {
    public PagamentoResponseDTO(Pagamento pagamento) {
        this(
                pagamento.getId(),
                pagamento.getChamado() != null ? pagamento.getChamado().getId() : null,
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getForma(),
                pagamento.getParcelas(),
                pagamento.getCartaoFinal(),
                pagamento.getCodigoPix(),
                pagamento.getDataCriacao(),
                pagamento.getDataPagamento()
        );
    }
}
