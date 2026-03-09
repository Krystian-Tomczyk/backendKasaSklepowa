package org.example.backendkasasklepowa.service;

import org.example.backendkasasklepowa.cart.SaleItem;
import org.example.backendkasasklepowa.cart.SaleRequest;
import org.example.backendkasasklepowa.model.Product;
import org.example.backendkasasklepowa.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductByBarcode(String barcode) {
        return productRepository.findById(barcode);
    }

    public Product saveProduct(Product product) {
        // Tutaj można dodać walidację, np. czy cena > 0
        return productRepository.save(product);
    }
    public Optional<Product> updateProduct(String barcode, Product productDetails) {
        return productRepository.findById(barcode).map(existingProduct -> {

            // aktualizujemy pola (NIE zmieniamy barcode, bo to klucz główny)
            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setStockQuantity(productDetails.getStockQuantity());

            return productRepository.save(existingProduct);
        });
    }
    // --- Dodawanie ilości w magazynie ---
    public Optional<Product> addQuantity(String ean, int amount) {
        return productRepository.findById(ean).map(product -> {
            int newQuantity = product.getStockQuantity() + amount; // stockQuantity w encji
            product.setStockQuantity(newQuantity);
            return productRepository.save(product);
        });
    }

    @Transactional
    public void processSale(List<SaleItem> items) {
    for (SaleItem item : items) {
        Product product = productRepository.findById(item.getBarcode()).orElseThrow(() -> new RuntimeException("Produkt nie istnieje: " + item.getBarcode()));
        if (product.getStockQuantity() < item.getQuantity()) {
            throw new RuntimeException("Brak towaru: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
        productRepository.save(product);
    }
}
}