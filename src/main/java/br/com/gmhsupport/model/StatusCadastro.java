package br.com.gmhsupport.model;

// pendente: aguardando o administrador analisar o CPF e a certidão de antecedentes
// aprovado: pode ver a fila e atender chamados
// recusado: vê o motivo e pode reenviar a certidão (volta para pendente)
public enum StatusCadastro {
    pendente,
    aprovado,
    recusado
}
