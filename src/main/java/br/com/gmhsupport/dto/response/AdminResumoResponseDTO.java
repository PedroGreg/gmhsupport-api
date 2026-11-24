package br.com.gmhsupport.dto.response;

import java.math.BigDecimal;

// Números do topo da área do administrador
public record AdminResumoResponseDTO(
        String nome,
        String email,
        Long tecnicosPendentes,
        Long repassesAPagar,
        BigDecimal totalAPagar,
        BigDecimal totalRepassado
) {
}
