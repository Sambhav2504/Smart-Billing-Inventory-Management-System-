package com.smartretail.backend.controller;
import jakarta.validation.constraints.Positive;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(201).body(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") String productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") String productId, @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        List<Product> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Product>> getExpiringProducts(@RequestParam("days") int days) {
        Date threshold = new Date(System.currentTimeMillis() + days * 24 * 60 * 60 * 1000L);
        List<Product> products = productService.getExpiringProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/restock/{id}")
    public ResponseEntity<Product> restockProduct(@PathVariable("id") String productId, @RequestBody RestockRequest request) {
        Product product = productService.restockProduct(productId, request.getRestockQty());
        return ResponseEntity.ok(product);
    }

    // Nested class for restock request
    public static class RestockRequest {
        @Positive(message = "Restock quantity must be positive")
        private int restockQty;

        public int getRestockQty() { return restockQty; }
        public void setRestockQty(int restockQty) { this.restockQty = restockQty; }
    }
}