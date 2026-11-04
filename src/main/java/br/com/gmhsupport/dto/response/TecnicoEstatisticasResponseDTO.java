package br.com.gmhsupport.dto.response;

import java.math.BigDecimal;

public record TecnicoEstatisticasResponseDTO(
        Long totalFinalizados,
        Double notaMedia,
        Long tempoMedioEmMinutos,
        BigDecimal valorRecebido
) {
}
