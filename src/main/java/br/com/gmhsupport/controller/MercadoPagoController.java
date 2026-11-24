package br.com.gmhsupport.controller;

import br.com.gmhsupport.integracao.MercadoPagoException;
import br.com.gmhsupport.integracao.WebhookAssinatura;
import br.com.gmhsupport.model.Pagamento;
import br.com.gmhsupport.services.PagamentoService;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.Optional;

// Rotas PÚBLICAS chamadas pelo Mercado Pago (sem token do app):
//  POST /pagamento/webhook  -> aviso de pagamento criado/atualizado
//  GET  /pagamento/retorno  -> para onde o Checkout Pro manda o cliente depois de pagar
// Nenhuma das duas confia nos dados recebidos: a API sempre consulta o pagamento no Mercado Pago.
@Hidden
@RestController
@RequestMapping("/pagamento")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    private final PagamentoService service;

    // Assinatura secreta do painel (Webhooks > Configurar notificações). Vazia = não confere a assinatura
    @Value("${gmh.mp.webhook-secret:}")
    private String segredoWebhook;

    public MercadoPagoController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam Map<String, String> params,
                                        @RequestBody(required = false) Map<String, Object> corpo,
                                        @RequestHeader(value = "x-signature", required = false) String assinatura,
                                        @RequestHeader(value = "x-request-id", required = false) String requestId) {
        String tipo = params.getOrDefault("type", params.get("topic"));
        String id = params.get("data.id");
        if (corpo != null) {
            if (tipo == null && corpo.get("type") != null) tipo = corpo.get("type").toString();
            if (id == null && corpo.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                id = data.get("id").toString();
            }
        }

        if (segredoWebhook != null && !segredoWebhook.isBlank()
                && !WebhookAssinatura.valida(assinatura, requestId, params.get("data.id"), segredoWebhook)) {
            logger.warn("Webhook com assinatura inválida (id {}) ignorado", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"payment".equals(tipo)) {
            return ResponseEntity.ok().build(); // outros avisos (merchant_order...) não interessam
        }

        try {
            service.processarAviso(id);
        } catch (MercadoPagoException e) {
            // 500 faz o Mercado Pago reenviar o aviso mais tarde
            logger.error("Webhook do pagamento {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    // O Mercado Pago acrescenta payment_id, status, external_reference... ao endereço
    @GetMapping(value = "/retorno", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> retorno(@RequestParam(required = false) String ref,
                                          @RequestParam(value = "payment_id", required = false) String paymentId,
                                          @RequestParam(value = "collection_status", required = false) String status) {
        Optional<Pagamento> pagamento = service.aoVoltarDoCheckout(ref, paymentId);

        String linkApp = pagamento
                .map(p -> "gmhsupport://pagamento?chamado=" + p.getChamado().getId() + "&tipo=" + p.getTipo())
                .orElse("gmhsupport://pagamento");
        boolean aprovado = pagamento.map(p -> p.getStatus().name().equals("pago")).orElse("approved".equals(status));
        String titulo = aprovado ? "Pagamento aprovado!" : "Pagamento ainda não concluído";
        String texto = aprovado
                ? "Pode voltar para o app GMH Support: seu chamado já está na fila."
                : "Volte para o app GMH Support para ver a situação ou tentar de novo.";

        String html = """
                <!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>GMH Support</title>
                <style>body{margin:0;font-family:sans-serif;background:#0E1113;color:#fff;display:flex;min-height:100vh;
                align-items:center;justify-content:center;text-align:center;padding:24px;box-sizing:border-box}
                a{display:inline-block;margin-top:24px;padding:14px 28px;background:#5980A6;color:#0E1113;
                text-decoration:none;font-weight:bold}p{color:#B3FFFFFF}</style></head><body><div>
                <h1>%s</h1><p>%s</p><a href="%s">VOLTAR PARA O APP</a></div>
                <script>setTimeout(function(){location.href="%s"},600)</script></body></html>
                """.formatted(HtmlUtils.htmlEscape(titulo), HtmlUtils.htmlEscape(texto),
                HtmlUtils.htmlEscape(linkApp), linkApp);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
