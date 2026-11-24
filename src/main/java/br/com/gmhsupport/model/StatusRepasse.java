package br.com.gmhsupport.model;

// nao_liberado: o serviço ainda não foi concluído (ou foi cancelado/estornado)
// liberado: chamado finalizado com o código do cliente; o admin precisa fazer o Pix ao técnico
// pago: o admin já fez o Pix para o técnico
public enum StatusRepasse {
    nao_liberado,
    liberado,
    pago
}
