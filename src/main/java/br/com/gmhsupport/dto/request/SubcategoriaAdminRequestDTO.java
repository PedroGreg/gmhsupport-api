package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubcategoriaAdminRequestDTO(
        @NotNull(message = "Informe a categoria") Long categoriaId,
        @NotBlank(message = "Informe o nome do problema")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String nome,
        @Size(max = 255) String descricao,
        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "1.00", message = "O valor mínimo é R$ 1,00")
        @DecimalMax(value = "10000.00", message = "O valor máximo é R$ 10.000,00")
        BigDecimal valorBase,
        Boolean ativo
) {
}
