package br.com.gmhsupport.model;

// pendente: aguardando o cliente pagar
// aguardando_pix: Pix gerado, esperando a confirmação do banco
// pago: pagamento confirmado
public enum StatusPagamento {
    pendente,
    aguardando_pix,
    pago
}
