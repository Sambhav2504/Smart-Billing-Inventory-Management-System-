package com.smartretail.backend.controller;

import com.smartretail.backend.dto.ProductRequest;
import com.smartretail.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // Spring will automatically inject the ProductService implementation here
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductRequest> getAllProducts() {
        return productService.getAllProducts(); // Delegate the work to the service
    }

    @GetMapping("/{id}")
    public ProductRequest getProductById(@PathVariable String id) {
        return productService.getProductById(id); // Delegate the work to the service
    }

    @PostMapping
    public String createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest); // Delegate the work to the service
    }

    @PutMapping("/{id}")
    public String updateProduct(@PathVariable String id, @RequestBody ProductRequest updateData) {
        return productService.updateProduct(id, updateData); // Delegate the work to the service
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id); // Delegate the work to the service
    }

    // Keep the debug endpoint for now, also delegated to the service
    @GetMapping("/debug")
    public List<ProductRequest> debugGetAllProducts() {
        return productService.getAllProducts();
    }
}