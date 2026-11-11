package br.com.gmhsupport.model;

import java.math.BigDecimal;

// O acréscimo é aplicado sobre o valor base da categoria:
// valor = valorBase * (1 + acrescimo)
public enum Prioridade {
    baixa("0.00"),
    media("0.20"),
    alta("0.50"),
    urgente("1.00");

    private final BigDecimal acrescimo;

    Prioridade(String acrescimo) {
        this.acrescimo = new BigDecimal(acrescimo);
    }

    public BigDecimal getAcrescimo() {
        return acrescimo;
    }
}
