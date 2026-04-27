package org.example.backendkasasklepowa.controller;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    // Uproszczony stan terminala (dla jednej kasy w sklepie)
    private String status = "IDLE"; // IDLE, WAITING_FOR_CARD, CARD_READ
    private BigDecimal currentAmount = BigDecimal.ZERO;
    private String lastCardUid = "";

    // 1. Kasa wywołuje to, żeby "obudzić" terminal
    @PostMapping("/initiate")
    public String initiatePayment(@RequestParam BigDecimal amount) {
        this.currentAmount = amount;
        this.lastCardUid = "";
        this.status = "WAITING_FOR_CARD";
        return "Terminal gotowy na " + amount + " PLN";
    }

    // 2. Terminal i Kasa sprawdzają co się dzieje
    @GetMapping("/status")
    public TerminalState getStatus() {
        return new TerminalState(status, currentAmount, lastCardUid);
    }

    // 3. Terminal wywołuje to, gdy klient zbliży kartę!
    @PostMapping("/cardRead")
    public String cardRead(@RequestParam String cardUid) {
        if ("WAITING_FOR_CARD".equals(this.status)) {
            this.lastCardUid = cardUid;
            this.status = "CARD_READ";
            System.out.println("Karta odczytana pomyślnie");
            return "Karta odczytana pomyślnie";
        }
        return "Terminal nie czekał na kartę";
    }

    // 4. Reset terminala po zakończeniu płatności
    @PostMapping("/clear")
    public void clearTerminal() {
        this.status = "IDLE";
        this.currentAmount = BigDecimal.ZERO;
        this.lastCardUid = "";
    }

    // Klasa pomocnicza (DTO) do zwracania statusu w JSON
    public static class TerminalState {
        public String status;
        public BigDecimal amount;
        public String cardUid;

        public TerminalState(String status, BigDecimal amount, String cardUid) {
            this.status = status;
            this.amount = amount;
            this.cardUid = cardUid;
        }
    }
}
