package br.com.gmhsupport.model;

public enum StatusAdicional {
    pendente,   // aguardando o cliente responder
    aceito,     // cliente aceitou (o pagamento do adicional segue em Pagamento, tipo "adicional")
    recusado    // cliente recusou: o técnico decide se continua ou cancela
}
