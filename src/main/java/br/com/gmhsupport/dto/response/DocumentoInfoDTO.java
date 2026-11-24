package br.com.gmhsupport.dto.response;

import java.time.LocalDateTime;

// Só os dados do arquivo (sem o conteúdo): para listar sem carregar megabytes do banco
public record DocumentoInfoDTO(
        Long tecnicoId,
        String nomeArquivo,
        String tipoConteudo,
        Long tamanho,
        LocalDateTime dataEnvio
) {
}
