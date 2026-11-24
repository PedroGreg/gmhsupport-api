package br.com.gmhsupport.dto.response;

import java.time.LocalDateTime;

// codigo só vai para o CLIENTE (GET /chamado/{id}/codigo). Para o técnico, codigo vem null.
public record CodigoConfirmacaoResponseDTO(
        String codigo,
        LocalDateTime expiraEm,
        Integer tentativasRestantes
) {
}
