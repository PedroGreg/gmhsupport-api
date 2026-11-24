package br.com.gmhsupport.dto.request;

import br.com.gmhsupport.model.DecisaoAdicional;
import jakarta.validation.constraints.NotNull;

// Depois da recusa do cliente: continuar pelo valor original ou cancelar (com estorno total)
public record AdicionalDecisaoRequestDTO(
        @NotNull(message = "Informe a decisão: continuar ou cancelar")
        DecisaoAdicional decisao
) {
}
