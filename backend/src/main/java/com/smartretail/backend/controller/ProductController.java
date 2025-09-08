package com.smartretail.backend.controller;

import com.smartretail.backend.dto.ProductRequest;
import com.smartretail.backend.service.ProductService;
import jakarta.validation.Valid;
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

    @GetMapping
    public ResponseEntity<List<ProductRequest>> getAllProducts() {
        List<ProductRequest> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRequest> getProductById(@PathVariable String id) {
        ProductRequest product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        String result = productService.createProduct(productRequest);
        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest updateData) {
        String result = productService.updateProduct(id, updateData);
        if (result.contains("Error")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        String result = productService.deleteProduct(id);
        if (result.contains("Error")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}