package br.com.gmhsupport.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

// Códigos de 6 dígitos do Google Authenticator (TOTP, RFC 6238): HMAC-SHA1, passo de 30 s.
// Feito à mão para não depender de biblioteca externa.
@Service
public class TotpService {

    public static final String EMISSOR = "GMH Support";
    private static final int PASSO_SEGUNDOS = 30;
    private static final int DIGITOS = 6;
    // Aceita o código do passo anterior e do próximo (relógio do celular um pouco adiantado/atrasado)
    private static final int JANELA = 1;
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom random = new SecureRandom();

    // 20 bytes aleatórios (160 bits) em base32 = 32 letras/números
    public String novoSegredo() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32(bytes);
    }

    // Link que o Authenticator entende (QR code ou toque no botão no celular)
    public String urlOtpauth(String segredo, String email) {
        String emissor = URLEncoder.encode(EMISSOR, StandardCharsets.UTF_8).replace("+", "%20");
        String conta = URLEncoder.encode(email, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + emissor + ":" + conta
                + "?secret=" + segredo
                + "&issuer=" + emissor
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PASSO_SEGUNDOS;
    }

    public long passoAtual() {
        return Instant.now().getEpochSecond() / PASSO_SEGUNDOS;
    }

    // Devolve o passo em que o código bate (para impedir reuso) ou -1 se o código estiver errado
    public long verificar(String segredo, String codigo, Long ultimoPassoUsado) {
        if (segredo == null || codigo == null || !codigo.matches("\\d{" + DIGITOS + "}")) return -1;
        byte[] chave = deBase32(segredo);
        long agora = passoAtual();
        for (int i = -JANELA; i <= JANELA; i++) {
            long passo = agora + i;
            if (ultimoPassoUsado != null && passo <= ultimoPassoUsado) continue;
            if (iguaisEmTempoConstante(gerar(chave, passo), codigo)) return passo;
        }
        return -1;
    }

    String gerar(byte[] chave, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(chave, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binario % (int) Math.pow(10, DIGITOS);
            return String.format("%0" + DIGITOS + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar código TOTP", e);
        }
    }

    private boolean iguaisEmTempoConstante(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(StandardCharsets.US_ASCII), b.getBytes(StandardCharsets.US_ASCII));
    }

    static String base32(byte[] dados) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte b : dados) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(BASE32.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) sb.append(BASE32.charAt((buffer << (5 - bits)) & 31));
        return sb.toString();
    }

    static byte[] deBase32(String texto) {
        String limpo = texto.replace("=", "").replace(" ", "").toUpperCase();
        ByteBuffer saida = ByteBuffer.allocate(limpo.length() * 5 / 8);
        int buffer = 0, bits = 0;
        for (char c : limpo.toCharArray()) {
            int valor = BASE32.indexOf(c);
            if (valor < 0) throw new IllegalArgumentException("Segredo inválido");
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                saida.put((byte) ((buffer >> (bits - 8)) & 0xFF));
                bits -= 8;
            }
        }
        return saida.array();
    }
}
