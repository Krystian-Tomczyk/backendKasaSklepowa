package org.example.backendkasasklepowa.controller;

import org.example.backendkasasklepowa.cart.SaleRequest;
import org.example.backendkasasklepowa.model.Product;
import org.example.backendkasasklepowa.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final RestTemplate restTemplate;
    private final String BLIK_URL = "http://localhost:8082/api/blik";

    public ProductController(ProductService productService, RestTemplate restTemplate) {
        this.productService = productService;
        this.restTemplate = restTemplate;
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

    // --- AKTUALIZACJA PRODUKTU ---
    // Przykład: PUT /api/products/update/590123456
    @PutMapping("/update/{barcode}")
    public ResponseEntity<Product> updateProduct(@PathVariable String barcode, @RequestBody Product productDetails) {
        System.out.println("=== Aktualizacja produktu: " + barcode + " ===");
        return productService.updateProduct(barcode, productDetails)
                .map(ResponseEntity::ok) // Jeśli znaleziono i zaktualizowano, zwróć 200 OK + produkt
                .orElse(ResponseEntity.notFound().build()); // Jeśli nie znaleziono kodu, zwróć 404 Not Found
    }

    // --- DOSTAWA (ZWIĘKSZENIE STANU) ---
    // Przykład: PATCH /api/products/addQuantity/5449000130389?amount=50
    @PatchMapping("/addQuantity/{ean}")
    public ResponseEntity<Product> addQuantity(@PathVariable String ean, @RequestParam int amount) {
        System.out.println("=== Dostawa: " + ean + " (+ " + amount + " szt.) ===");

        return productService.addQuantity(ean, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DODAWANIE PRODUKTU ---
    // Przykład: POST /api/products z ciałem JSON
    @PostMapping
    public Product addProduct(@RequestBody Product product) {

        System.out.println("=== Odebrany produkt ===");
        System.out.println("Kod kreskowy: " + product.getBarcode());
        System.out.println("Nazwa: " + product.getName());
        System.out.println("Opis: " + product.getDescription());
        System.out.println("Cena: " + product.getPrice());
        System.out.println("Ilość na stanie: " + product.getStockQuantity());
        System.out.println("========================");

        return productService.saveProduct(product);
    }

    @PostMapping("/sale")public ResponseEntity<?> finalizeSale(@RequestBody SaleRequest request) {
        try {
            productService.processSale(request.getItems());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // --- ENDPOINTY POŚREDNICZĄCE DLA KASY WPF ---

    @PostMapping("/blik/initiate")
    public ResponseEntity<String> initiateBlik(@RequestParam String code, @RequestParam BigDecimal amount, @RequestParam String storeName) {
        String url = BLIK_URL + "/initiate?code=" + code + "&amount=" + amount + "&storeName=" + storeName;
        return restTemplate.postForEntity(url, null, String.class);
    }

    @GetMapping("/blik/status/{code}")
    public ResponseEntity<String> checkBlikStatus(@PathVariable String code) {
        String url = BLIK_URL + "/status/" + code;
        return restTemplate.getForEntity(url, String.class);
    }


}
