package br.com.gmhsupport.dto.response;

import br.com.gmhsupport.model.FormaPagamento;
import br.com.gmhsupport.model.MeioPagamento;
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
        StatusPagamento status,        // pendente (cartão: abrir o link) | aguardando_pix | pago | estornado
        MeioPagamento meio,            // pix | checkout (cartão pelo Mercado Pago)
        FormaPagamento forma,          // como foi pago de fato (preenchido na aprovação no cartão)
        Integer parcelas,
        String cartaoFinal,
        String bandeira,
        String codigoPix,              // Pix "copia e cola" (o app desenha o QR code com ele)
        LocalDateTime pixExpiraEm,
        String linkPagamento,          // Checkout Pro: página do Mercado Pago para pagar com cartão
        String mensagem,               // explicação para o cliente (ex.: "Cartão recusado: saldo insuficiente")
        LocalDateTime dataCriacao,
        LocalDateTime dataPagamento
) {
    public PagamentoResponseDTO(Pagamento p) {
        this(
                p.getId(),
                p.getChamado() != null ? p.getChamado().getId() : null,
                p.getTipo(),
                p.getValor(),
                p.getValorServico(),
                p.getValorDeslocamento(),
                p.getStatus(),
                p.getMeio(),
                p.getForma(),
                p.getParcelas(),
                p.getCartaoFinal(),
                p.getBandeira(),
                p.getStatus() == StatusPagamento.aguardando_pix ? p.getCodigoPix() : null,
                p.getPixExpiraEm(),
                p.getStatus() == StatusPagamento.pendente ? p.getLinkPagamento() : null,
                mensagem(p),
                p.getDataCriacao(),
                p.getDataPagamento()
        );
    }

    // Texto amigável a partir do status do Mercado Pago
    static String mensagem(Pagamento p) {
        if (p.getStatus() == StatusPagamento.pago) return "Pagamento aprovado.";
        if (p.getStatus() == StatusPagamento.estornado) return "Pagamento devolvido ao cliente.";
        String status = p.getMpStatus() == null ? "" : p.getMpStatus();
        String detalhe = p.getMpStatusDetalhe() == null ? "" : p.getMpStatusDetalhe();
        if (p.getStatus() == StatusPagamento.aguardando_pix) {
            boolean expirou = "cancelled".equals(status) || "expired".equals(detalhe)
                    || (p.getPixExpiraEm() != null && p.getPixExpiraEm().isBefore(LocalDateTime.now()));
            return expirou ? "O Pix expirou. Gere um novo código." : "Aguardando o Pix.";
        }
        return switch (status) {
            case "rejected" -> switch (detalhe) {
                case "cc_rejected_insufficient_amount" -> "Cartão recusado: limite ou saldo insuficiente.";
                case "cc_rejected_bad_filled_security_code" -> "Cartão recusado: código de segurança incorreto.";
                case "cc_rejected_bad_filled_date" -> "Cartão recusado: data de validade incorreta.";
                case "cc_rejected_bad_filled_card_number", "cc_rejected_bad_filled_other" -> "Cartão recusado: confira os dados do cartão.";
                case "cc_rejected_call_for_authorize" -> "Cartão recusado: autorize o pagamento com o seu banco.";
                case "cc_rejected_high_risk" -> "Pagamento recusado por segurança. Tente outro cartão ou o Pix.";
                case "cc_rejected_duplicated_payment" -> "Pagamento recusado: parece duplicado.";
                default -> "Pagamento recusado. Tente de novo, com outro cartão ou com Pix.";
            };
            case "in_process", "pending" -> "Pagamento em análise pelo Mercado Pago. Aguarde alguns minutos.";
            case "cancelled" -> "Pagamento cancelado. Tente de novo.";
            default -> "Aguardando o pagamento.";
        };
    }
}
