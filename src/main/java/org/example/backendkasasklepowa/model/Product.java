package org.example.backendkasasklepowa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data // Lombok: generuje gettery, settery, toString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kod kreskowy (ważne dla skanera)
    @Column(unique = true, nullable = false)
    private String barcode;

    private String name;

    private String description;

    // Cena (BigDecimal jest lepszy do pieniędzy niż double)
    private BigDecimal price;

    // Ilość na stanie
    private int stockQuantity;
}