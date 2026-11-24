package br.com.gmhsupport.model;

// Decisão do técnico depois que o cliente recusa o adicional
public enum DecisaoAdicional {
    continuar,  // segue pelo valor original, fazendo o possível
    cancelar    // encerra o chamado e estorna tudo ao cliente
}
