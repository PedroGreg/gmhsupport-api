package br.com.gmhsupport.dto.response;

import java.math.BigDecimal;

public record RepassePagoResponseDTO(
        Long tecnicoId,
        String tecnicoNome,
        String chavePix,
        Integer quantidade,
        BigDecimal total
) {
}
