package org.example.backendkasasklepowa.payment;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String code;
    private BigDecimal amount;
    private String storeName;
    private String correlationId; // Unikalne ID transakcji, wygenerowane przez WPF

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}