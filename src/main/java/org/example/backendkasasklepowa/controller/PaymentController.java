package org.example.backendkasasklepowa.controller;

import org.example.backendkasasklepowa.config.TerminalWebSocketHandler;
import org.example.backendkasasklepowa.payment.PaymentProcessor;
import org.example.backendkasasklepowa.payment.PaymentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProcessor blikProcessor;
    private final PaymentProcessor trustPayProcessor;

    @Value("${trustpay.webhook.secret}")
    private String webhookSecret;

    // Automatycznie wstrzykujemy wybrane strategie po nazwach (zdefiniowanych w @Service)
    public PaymentController(
            @Qualifier("blikProcessor") PaymentProcessor blikProcessor,
            @Qualifier("trustPayProcessor") PaymentProcessor trustPayProcessor) {
        this.blikProcessor = blikProcessor;
        this.trustPayProcessor = trustPayProcessor;
    }

    // --- ENDPOINT INICJUJĄCY (Używany z kasy WPF) ---
    @PostMapping("/initiate/{method}")
    public ResponseEntity<String> initiatePayment(@PathVariable String method, @RequestBody PaymentRequest request) {
        if ("BLIK".equalsIgnoreCase(method)) {
            blikProcessor.processPayment(request);
        } else if ("TRUSTPAY".equalsIgnoreCase(method)) {
            trustPayProcessor.processPayment(request);
        } else {
            return ResponseEntity.badRequest().body("Nieznana metoda płatności");
        }

        // Zwracamy PENDING od razu. Prawdziwy status przyjdzie z WebSocketa!
        return ResponseEntity.ok("PENDING");
    }

    // --- WEBHOOK DLA TRUSTPAY (Uderza tutaj chmura zewnętrzna) ---
    @PostMapping("/webhook/trustpay/{correlationId}")
    public ResponseEntity<String> handleTrustPayWebhook(
            @PathVariable String correlationId,
            @RequestHeader("x-webhook-signature") String signature,
            @RequestBody byte[] rawBody) {

        String payload = new String(rawBody, StandardCharsets.UTF_8);

        // 1. Walidacja bezpieczeństwa HMAC SHA-256
        if (!verifySignature(payload, signature, webhookSecret)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        try {
            // 2. Wyciągnięcie statusu z JSONa bez skomplikowanych parserów (szybki pars Stringa)
            String status = "FAILED";
            if (payload.contains("\"status\":\"CONFIRMED\"")) status = "COMPLETED"; // Mapowanie by pasowało do logiki
            else if (payload.contains("\"status\":\"REJECTED\"")) status = "REJECTED";
            else if (payload.contains("\"status\":\"EXPIRED\"")) status = "EXPIRED";

            // 3. Wypchnięcie wyniku na Kase przez WebSocket
            TerminalWebSocketHandler.broadcastMessage(
                    String.format("{\"action\":\"PAYMENT_RESULT\", \"correlationId\":\"%s\", \"status\":\"%s\"}", correlationId, status)
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Algorytm zgodny z dokumentacją TrustPay
    private boolean verifySignature(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Formatowanie hash-a na format HEX i porównanie bez uwzględnienia wielkości liter
            String expected = HexFormat.of().formatHex(hash);
            return expected.equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}