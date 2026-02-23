package org.example.backendkasasklepowa.controller;

import org.example.backendkasasklepowa.model.Product;
import org.example.backendkasasklepowa.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // --- API TESTOWE ---
    @GetMapping("/test")
    public String testConnection() {
        return "Połączenie z backendem działa poprawnie! " + java.time.LocalDateTime.now();
    }

    // --- POBIERANIE WSZYSTKICH PRODUKTÓW ---
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // --- POBIERANIE PO KODZIE KRESKOWYM (Symulacja skanowania) ---
    // Przykład: GET /api/products/scan/590123456
    @GetMapping("/scan/{barcode}")
    public ResponseEntity<Product> getProductByBarcode(@PathVariable String barcode) {
        return productService.getProductByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DODAWANIE PRODUKTU ---
    // Przykład: POST /api/products z ciałem JSON
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }
}