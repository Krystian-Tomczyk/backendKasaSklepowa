package org.example.backendkasasklepowa.cart;

import java.util.List;

public class SaleRequest {
    private List<SaleItem> items;

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
}
