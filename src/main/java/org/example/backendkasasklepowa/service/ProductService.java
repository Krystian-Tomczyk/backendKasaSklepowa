package org.example.backendkasasklepowa.service;

import org.example.backendkasasklepowa.model.Product;
import org.example.backendkasasklepowa.repository.ProductRepository;
import org.springframework.stereotype.Service;

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
}