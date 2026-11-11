package br.com.gmhsupport.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import jakarta.validation.constraints.Size;

public record CategoriaProblemaRequestDTO(
        @NotNull(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        // Opcional: se não vier, mantém o valor atual (ou R$ 60,00 em categorias novas)
        @DecimalMin(value = "0.00", message = "O valor base não pode ser negativo")
        BigDecimal valorBase
){
}
