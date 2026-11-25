package br.com.gmhsupport.integracao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// Chamadas HTTP à API do Mercado Pago (https://api.mercadopago.com).
// O Access Token vem da variável MP_ACCESS_TOKEN (Render) e NUNCA vai para o app.
// Respostas lidas como Map: só usamos alguns campos, sem precisar do SDK do Mercado Pago.
@Component
public class MercadoPagoClient {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {};

    private final RestClient http;
    private final boolean configurado;

    public MercadoPagoClient(@Value("${gmh.mp.access-token:}") String accessToken) {
        this.configurado = accessToken != null && !accessToken.isBlank();
        // HttpClient do Java: ao contrário do HttpURLConnection, lê o corpo das respostas 401
        // (assim o log mostra o motivo que o Mercado Pago deu)
        java.net.http.HttpClient cliente = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(25));
        this.http = RestClient.builder()
                .baseUrl("https://api.mercadopago.com")
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (configurado ? accessToken.trim() : ""))
                .build();
        if (!configurado) {
            logger.warn("MP_ACCESS_TOKEN não definido: pagamentos do Mercado Pago desligados.");
        } else {
            // Só o começo e o tamanho (nunca o token inteiro): ajuda a ver se a variável foi colada certa
            String t = accessToken.trim();
            logger.info("Mercado Pago: token {}... ({} caracteres)", t.substring(0, Math.min(12, t.length())), t.length());
        }
    }

    public boolean configurado() {
        return configurado;
    }

    // POST /v1/payments (Pix). Devolve o pagamento criado (id, status, point_of_interaction...)
    public Map<String, Object> criarPagamento(Map<String, Object> corpo, String idempotencia) {
        return chamar("criar Pix", () -> http.post().uri("/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", idempotencia)
                .body(corpo).retrieve().body(MAPA));
    }

    // POST /checkout/preferences (Checkout Pro). Devolve id e init_point (link da página de pagamento)
    public Map<String, Object> criarPreferencia(Map<String, Object> corpo) {
        return chamar("criar preferência", () -> http.post().uri("/checkout/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo).retrieve().body(MAPA));
    }

    // GET /v1/payments/{id}: a fonte da verdade sobre um pagamento
    public Map<String, Object> buscarPagamento(String id) {
        return chamar("consultar pagamento", () -> http.get().uri("/v1/payments/{id}", id)
                .retrieve().body(MAPA));
    }

    // GET /v1/payments/search?external_reference=...: pagamentos feitos pelo Checkout Pro
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarPorReferencia(String referencia) {
        Map<String, Object> r = chamar("buscar pagamentos", () -> http.get()
                .uri(u -> u.path("/v1/payments/search")
                        .queryParam("external_reference", referencia)
                        .queryParam("sort", "date_created")
                        .queryParam("criteria", "desc")
                        .build())
                .retrieve().body(MAPA));
        Object resultados = r != null ? r.get("results") : null;
        return resultados instanceof List<?> l ? (List<Map<String, Object>>) l : List.of();
    }

    // POST /v1/payments/{id}/refunds (estorno total). A mesma chave de idempotência evita estorno em dobro
    public Map<String, Object> estornar(String id) {
        return chamar("estornar", () -> http.post().uri("/v1/payments/{id}/refunds", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "gmh-estorno-" + id)
                .body(Map.of()).retrieve().body(MAPA));
    }

    // PUT /v1/payments/{id} {status: cancelled}: cancela um Pix ainda não pago
    public void cancelar(String id) {
        chamar("cancelar Pix", () -> http.put().uri("/v1/payments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "cancelled")).retrieve().body(MAPA));
    }

    private Map<String, Object> chamar(String acao, Supplier<Map<String, Object>> chamada) {
        if (!configurado) {
            throw new MercadoPagoException("Pagamentos indisponíveis: o Mercado Pago não está configurado na API.");
        }
        try {
            return chamada.get();
        } catch (RestClientResponseException e) {
            logger.error("Mercado Pago recusou '{}' (HTTP {}): {}", acao, e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            // Mostra o motivo do Mercado Pago (ex.: "payer.email must be a valid email"): ajuda a achar o problema
            String motivo = motivo(e.getResponseBodyAsString());
            throw new MercadoPagoException("O Mercado Pago recusou a operação (" + acao + ")"
                    + (motivo != null ? ": " + motivo : ". Tente de novo em instantes."));
        } catch (ResourceAccessException e) {
            logger.error("Sem resposta do Mercado Pago em '{}': {}", acao, e.getMessage());
            throw new MercadoPagoException("O Mercado Pago não respondeu. Tente de novo em instantes.");
        }
    }

    // Tira "message" e a "description" da primeira "cause" do JSON de erro do Mercado Pago
    private static String motivo(String corpo) {
        if (corpo == null || corpo.isBlank()) return null;
        java.util.List<String> partes = new java.util.ArrayList<>();
        for (String chave : new String[]{"message", "description"}) {
            var m = java.util.regex.Pattern.compile("\"" + chave + "\"\\s*:\\s*\"([^\"]{1,200})\"").matcher(corpo);
            if (m.find() && !partes.contains(m.group(1))) partes.add(m.group(1));
        }
        return partes.isEmpty() ? null : String.join(" — ", partes);
    }

    // ----- leitura segura de campos do JSON -----

    public static Object campo(Map<String, Object> mapa, String... caminho) {
        Object atual = mapa;
        for (String chave : caminho) {
            if (!(atual instanceof Map<?, ?> m)) return null;
            atual = m.get(chave);
        }
        return atual;
    }

    public static String texto(Map<String, Object> mapa, String... caminho) {
        Object v = campo(mapa, caminho);
        if (v == null) return null;
        // ids numéricos chegam como Long/Double: sem ".0"
        if (v instanceof Number n && n.doubleValue() == Math.floor(n.doubleValue())) return String.valueOf(n.longValue());
        return v.toString();
    }
}
