package br.com.gmhsupport.integracao;

// Erro ao falar com o Mercado Pago. É um IllegalStateException: o CustomExceptionHandler
// devolve 409 com a mensagem, e a transação (ex.: abrir o chamado) é desfeita.
public class MercadoPagoException extends IllegalStateException {
    public MercadoPagoException(String mensagem) {
        super(mensagem);
    }
}
