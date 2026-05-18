package org.example.backendkasasklepowa.payment;

import org.example.backendkasasklepowa.config.TerminalWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("blikProcessor")
public class BlikPaymentProcessor implements PaymentProcessor {

    private final RestTemplate restTemplate;

    @Value("${blik.url}")
    private String blikUrl;

    public BlikPaymentProcessor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Async // Uruchamia logikę w nowym wątku! Kasa dostaje odpowiedź 200 OK od razu.
    public void processPayment(PaymentRequest request) {
        try {
            // 1. Zgłoszenie płatności do BLIKa
            String initUrl = blikUrl + "/initiate?code=" + request.getCode() + "&amount=" + request.getAmount() + "&storeName=" + request.getStoreName();
            restTemplate.postForEntity(initUrl, null, String.class);

            // 2. Wewnętrzna pętla zamiast odpytywania z WPF
            int attempts = 0;
            while (attempts < 60) { // Oczekujemy max 2 minuty (60 * 2s)
                Thread.sleep(2000);

                String statusUrl = blikUrl + "/status/" + request.getCode();
                String status = restTemplate.getForObject(statusUrl, String.class);

                if ("COMPLETED".equals(status) || "REJECTED".equals(status) || "FAILED".equals(status) || "EXPIRED".equals(status)) {
                    // Mamy ostateczny wynik - wypychamy po WebSocket do kasy!
                    TerminalWebSocketHandler.broadcastMessage(
                            String.format("{\"action\":\"PAYMENT_RESULT\", \"correlationId\":\"%s\", \"status\":\"%s\"}", request.getCorrelationId(), status)
                    );
                    return; // Koniec pracy wątku
                }
                attempts++;
            }

            // Timeout
            TerminalWebSocketHandler.broadcastMessage(
                    String.format("{\"action\":\"PAYMENT_RESULT\", \"correlationId\":\"%s\", \"status\":\"EXPIRED\"}", request.getCorrelationId())
            );

        } catch (Exception e) {
            System.err.println("Błąd asynchronicznego procesowania BLIK: " + e.getMessage());
            TerminalWebSocketHandler.broadcastMessage(
                    String.format("{\"action\":\"PAYMENT_RESULT\", \"correlationId\":\"%s\", \"status\":\"FAILED\"}", request.getCorrelationId())
            );
        }
    }
}