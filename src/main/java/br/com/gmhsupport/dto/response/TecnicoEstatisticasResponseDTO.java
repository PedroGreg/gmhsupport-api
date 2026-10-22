package br.com.gmhsupport.dto.response;

public record TecnicoEstatisticasResponseDTO(
        Long totalFinalizados,
        Double notaMedia,
        Long tempoMedioEmMinutos
) {
}
