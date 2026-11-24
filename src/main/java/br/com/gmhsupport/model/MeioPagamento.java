package br.com.gmhsupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

// Como o cliente ESCOLHEU pagar (a forma real — pix, crédito ou débito — chega na aprovação):
//  pix      -> a API gera o QR code / "copia e cola" no Mercado Pago e o app mostra
//  checkout -> Checkout Pro: o app abre a página do Mercado Pago e o cliente paga com cartão lá
public enum MeioPagamento {
    pix,
    checkout;

    // O app manda "pix" ou "cartao". Aceita também "credito"/"debito" (versões antigas do app).
    @JsonCreator
    public static MeioPagamento de(String valor) {
        if (valor == null) return null;
        return switch (valor.trim().toLowerCase()) {
            case "pix" -> pix;
            case "checkout", "cartao", "credito", "debito" -> checkout;
            default -> throw new IllegalArgumentException("Forma de pagamento inválida: use pix ou cartao.");
        };
    }
}
