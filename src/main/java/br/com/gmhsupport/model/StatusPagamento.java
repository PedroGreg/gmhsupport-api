package br.com.gmhsupport.model;

// pendente: aguardando o cliente pagar
// aguardando_pix: Pix gerado, esperando a confirmação do banco
// pago: pagamento confirmado
// estornado: chamado cancelado depois de pago, o valor foi devolvido
public enum StatusPagamento {
    pendente,
    aguardando_pix,
    pago,
    estornado
}
