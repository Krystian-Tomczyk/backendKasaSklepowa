package org.example.backendkasasklepowa.payment;

public interface PaymentProcessor {
    // Rozpoczyna płatność i od razu zwraca odpowiedź do kasy.
    // Dalsze przetwarzanie odbywa się w tle.
    void processPayment(PaymentRequest request);
}