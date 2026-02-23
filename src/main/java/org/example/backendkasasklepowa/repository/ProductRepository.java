package org.example.backendkasasklepowa.repository;

import org.example.backendkasasklepowa.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Metoda do szukania po kodzie kreskowym (dla skanera)
    Optional<Product> findByBarcode(String barcode);
}