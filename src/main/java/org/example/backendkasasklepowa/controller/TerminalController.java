package org.example.backendkasasklepowa.controller;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private String status = "IDLE";
    // Statusy: IDLE, WAITING_FOR_CARD, CARD_READ, SUCCESS, REJECTED_PIN, REJECTED_FUNDS, REJECTED_GENERAL

    private BigDecimal currentAmount = BigDecimal.ZERO;
    private String lastCardUid = "";
    private String lastPin = ""; // Przechowuje PIN z terminala

    @PostMapping("/initiate")
    public String initiatePayment(@RequestParam BigDecimal amount) {
        this.currentAmount = amount;
        this.lastCardUid = "";
        this.lastPin = "";
        this.status = "WAITING_FOR_CARD";
        return "Terminal gotowy";
    }

    @GetMapping("/status")
    public TerminalState getStatus() {
        return new TerminalState(status, currentAmount, lastCardUid, lastPin);
    }

    // Terminal wysyła tu kartę i opcjonalnie PIN
    @PostMapping("/cardRead")
    public String cardRead(
            @RequestParam String cardUid,
            @RequestParam(required = false) String pin) {

        if ("WAITING_FOR_CARD".equals(this.status)) {
            this.lastCardUid = cardUid;
            this.lastPin = pin;
            this.status = "CARD_READ"; // Kasa WPF to wyłapie!
            return "OK";
        }
        return "Terminal nie czekał";
    }

    // Kasa WPF po kontakcie z bankiem wysyła tu werdykt dla terminala
    @PostMapping("/setResult")
    public void setResult(@RequestParam String result) {
        this.status = result;
    }

    @PostMapping("/clear")
    public void clearTerminal() {
        this.status = "IDLE";
        this.currentAmount = BigDecimal.ZERO;
        this.lastCardUid = "";
        this.lastPin = "";
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