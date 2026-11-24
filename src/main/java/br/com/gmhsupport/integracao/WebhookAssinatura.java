package br.com.gmhsupport.integracao;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

// Confere o cabeçalho x-signature dos webhooks do Mercado Pago:
//   x-signature: ts=1704908010,v1=<hmac>
//   mensagem assinada: "id:<data.id>;request-id:<x-request-id>;ts:<ts>;"
//   v1 = HMAC-SHA256(mensagem, assinatura secreta do painel) em hexadecimal
public final class WebhookAssinatura {

    private WebhookAssinatura() {
    }

    public static boolean valida(String xSignature, String xRequestId, String dataId, String segredo) {
        if (xSignature == null || segredo == null || segredo.isBlank()) return false;
        String ts = null, v1 = null;
        for (String parte : xSignature.split(",")) {
            String[] kv = parte.trim().split("=", 2);
            if (kv.length != 2) continue;
            if (kv[0].trim().equals("ts")) ts = kv[1].trim();
            if (kv[0].trim().equals("v1")) v1 = kv[1].trim();
        }
        if (ts == null || v1 == null) return false;

        StringBuilder msg = new StringBuilder();
        if (dataId != null && !dataId.isBlank()) {
            // ids alfanuméricos são assinados em minúsculas
            msg.append("id:").append(dataId.matches("[0-9]+") ? dataId : dataId.toLowerCase()).append(';');
        }
        if (xRequestId != null && !xRequestId.isBlank()) msg.append("request-id:").append(xRequestId).append(';');
        msg.append("ts:").append(ts).append(';');

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String esperado = HexFormat.of().formatHex(mac.doFinal(msg.toString().getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.US_ASCII),
                    v1.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            return false;
        }
    }
}
