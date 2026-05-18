package org.example.backendkasasklepowa.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service("trustPayProcessor")
public class TrustPayPaymentProcessor implements PaymentProcessor {

    private final RestTemplate restTemplate;

    @Value("${trustpay.api.url}")
    private String trustPayApiUrl;

    @Value("${trustpay.webhook.public-url}")
    private String webhookPublicUrl;

    @Value("${trustpay.webhook.secret}")
    private String webhookSecret;

    public TrustPayPaymentProcessor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void processPayment(PaymentRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", request.getCode());
        body.put("amount", request.getAmount());
        body.put("storeName", request.getStoreName());
        body.put("webhookUrl", webhookPublicUrl + "/api/payments/webhook/trustpay/" + request.getCorrelationId());
        body.put("webhookSecret", webhookSecret);

        try {
            // Strzał do API TrustPay.
            // Płatność zostanie obsłużona asynchronicznie, a odpowiedź wróci na Webhook.
            restTemplate.postForEntity(trustPayApiUrl, body, String.class);
            System.out.println("Zlecono płatność TrustPay. Oczekuję na Webhook...");
        } catch (Exception e) {
            System.err.println("Błąd komunikacji z TrustPay: " + e.getMessage());
            // Jeśli od razu uderzenie w serwer się nie uda, powiadamiamy kasę o błędzie
            org.example.backendkasasklepowa.config.TerminalWebSocketHandler.broadcastMessage(
                    String.format("{\"action\":\"PAYMENT_RESULT\", \"correlationId\":\"%s\", \"status\":\"FAILED\"}", request.getCorrelationId())
            );
        }
    }
}