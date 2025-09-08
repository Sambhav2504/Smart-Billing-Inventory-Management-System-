package com.smartretail.backend.controller;

import com.smartretail.backend.models.Product;
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
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(201).body(savedProduct);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @Valid @RequestBody Product updateData) {
        Product updatedProduct = productService.updateProduct(productId, updateData);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}