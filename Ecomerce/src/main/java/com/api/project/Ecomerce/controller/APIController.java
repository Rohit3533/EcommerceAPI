package com.api.project.Ecomerce.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class APIController {

    private static final String API = "https://dummyjson.com/products?limit=100";

    @GetMapping("/{category}")
    public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(
            @PathVariable String category) {

        RestTemplate restTemplate = new RestTemplate();

        // Call external API
        Map<String, Object> response =
                restTemplate.getForObject(API, Map.class);

        if (response == null || !response.containsKey("products")) {
            return ResponseEntity.internalServerError().build();
        }

        List<Map<String, Object>> products =
                (List<Map<String, Object>>) response.get("products");

        // Filter by category
        List<Map<String, Object>> filteredProducts = products.stream()
                .filter(p -> category.equalsIgnoreCase(
                        String.valueOf(p.get("category"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredProducts);
    }
    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getAllCategories() {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response =
                restTemplate.getForObject(API, Map.class);

        if (response == null || !response.containsKey("products")) {
            return ResponseEntity.internalServerError().build();
        }

        List<Map<String, Object>> products =
                (List<Map<String, Object>>) response.get("products");

        Set<String> categories = products.stream()
                .map(p -> String.valueOf(p.get("category")))
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
     log.info("{}{}","successfully added the categories",categories);
        return ResponseEntity.ok(categories);
    }

}
