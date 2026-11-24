package br.com.gmhsupport.dto.response;

// Etapa 1 do login do admin (senha certa).
// configurar = true: primeiro acesso, o app mostra o QR code / segredo para adicionar no Google Authenticator.
public record AdminLoginResponseDTO(
        String tokenTemporario,
        Boolean configurar,
        String segredo,
        String otpauthUrl,
        String emissor,
        String mensagem
) {
}
