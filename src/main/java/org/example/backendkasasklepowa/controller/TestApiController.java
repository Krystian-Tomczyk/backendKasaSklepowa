package org.example.backendkasasklepowa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestApiController {
    @GetMapping("/test")
    public String testApi() {
        System.out.println("Test połączenia z api - połączenie nawiązane");
        return "API działa poprawnie – połączenie nawiązane.";
    }
}