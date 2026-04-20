package org.example.backendkasasklepowa.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private String status = "IDLE";
    // Statusy: IDLE, WAITING_FOR_CARD, CARD_READ, SUCCESS, REJECTED_PIN, REJECTED_FUNDS, REJECTED_GENERAL

    private BigDecimal currentAmount = BigDecimal.ZERO;
    private String lastCardUid = "";
    private String lastPin = "";

    // Lista zarejestrowanych webhooków (stateless — tylko URL-e, zero otwartych połączeń)
    private final List<String> webhookUrls = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // --- Rejestracja/wyrejestrowanie webhooków ---

    @PostMapping("/registerWebhook")
    public String registerWebhook(@RequestParam String url) {
        if (!webhookUrls.contains(url)) {
            webhookUrls.add(url);
            System.out.println("Zarejestrowano webhook: " + url + " (łącznie: " + webhookUrls.size() + ")");
        }
        // Wyślij aktualny stan natychmiast po rejestracji
        notifyWebhook(url, buildState());
        return "OK";
    }

    @PostMapping("/unregisterWebhook")
    public String unregisterWebhook(@RequestParam String url) {
        webhookUrls.remove(url);
        System.out.println("Wyrejestrowano webhook: " + url + " (pozostało: " + webhookUrls.size() + ")");
        return "OK";
    }

    // --- Powiadomienie wszystkich zarejestrowanych webhooków ---
    private void notifyAllWebhooks() {
        TerminalState state = buildState();
        for (String url : webhookUrls) {
            notifyWebhook(url, state);
        }
    }

    private boolean notifyWebhook(String url, TerminalState state) {
        // Fire-and-forget POST do webhooka w osobnym wątku
        new Thread(() -> {
            try {
                String json = objectMapper.writeValueAsString(state);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(json, headers);

                restTemplate.postForEntity(url, entity, String.class);
            } catch (Exception e) {
                System.out.println("Webhook niedostępny, usuwam: " + url + " (" + e.getMessage() + ")");
                webhookUrls.remove(url);
            }
        }).start();
        return true;
    }

    private TerminalState buildState() {
        return new TerminalState(status, currentAmount, lastCardUid, lastPin);
    }

    // --- Endpointy biznesowe ---

    @PostMapping("/initiate")
    public String initiatePayment(@RequestParam BigDecimal amount) {
        this.currentAmount = amount;
        this.lastCardUid = "";
        this.lastPin = "";
        this.status = "WAITING_FOR_CARD";
        notifyAllWebhooks();
        return "Terminal gotowy";
    }

    @GetMapping("/status")
    public TerminalState getStatus() {
        return buildState();
    }

    @PostMapping("/cardRead")
    public String cardRead(
            @RequestParam String cardUid,
            @RequestParam(required = false) String pin) {

        if ("WAITING_FOR_CARD".equals(this.status)) {
            this.lastCardUid = cardUid;
            this.lastPin = pin != null ? pin : "";
            this.status = "CARD_READ";
            notifyAllWebhooks();
            return "OK";
        }
        return "Terminal nie czekał";
    }

    @PostMapping("/setResult")
    public void setResult(@RequestParam String result) {
        this.status = result;
        notifyAllWebhooks();
    }

    @PostMapping("/clear")
    public void clearTerminal() {
        this.status = "IDLE";
        this.currentAmount = BigDecimal.ZERO;
        this.lastCardUid = "";
        this.lastPin = "";
        notifyAllWebhooks();
    }

    public static class TerminalState {
        public String status;
        public BigDecimal amount;
        public String cardUid;
        public String pin;

        public TerminalState(String status, BigDecimal amount, String cardUid, String pin) {
            this.status = status;
            this.amount = amount;
            this.cardUid = cardUid;
            this.pin = pin;
        }
    }
}