package br.com.gmhsupport.dto.response;

import java.math.BigDecimal;

// Quanto o chamado vai custar, peça por peça (mostrado no app antes de abrir)
public record OrcamentoResponseDTO(
        Long subcategoriaId,
        String subcategoriaNome,
        BigDecimal valorBase,            // preço da subcategoria
        Integer acrescimoPercentual,     // pela prioridade (0, 20, 50 ou 100)
        BigDecimal valorServico,         // valorBase + acréscimo
        BigDecimal distanciaKm,          // em linha reta, a partir do SENAC Jurubatuba
        BigDecimal kmGratis,
        BigDecimal kmCobrados,
        BigDecimal precoPorKm,
        BigDecimal valorDeslocamento,
        BigDecimal total,
        BigDecimal distanciaMaximaKm,
        boolean dentroDaArea,            // false = mais longe que a distância máxima: não dá para abrir
        boolean valorFixo                // categoria de preço fixo (ex.: Testes)
) {
}
