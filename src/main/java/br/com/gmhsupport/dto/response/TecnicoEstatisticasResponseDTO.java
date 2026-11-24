package br.com.gmhsupport.dto.response;

import java.math.BigDecimal;

public record TecnicoEstatisticasResponseDTO(
        Long totalFinalizados,
        Double notaMedia,
        Long tempoMedioEmMinutos,
        BigDecimal valorRecebido,   // repasses que o admin já pagou (Pix feito)
        BigDecimal valorAReceber,   // repasses liberados, aguardando o Pix do admin
        Integer percentualRepasse   // ex.: 80
) {
}
