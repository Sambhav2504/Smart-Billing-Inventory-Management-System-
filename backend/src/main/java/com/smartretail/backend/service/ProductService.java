package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import java.util.List;

// This interface defines the contract for all product-related operations.
public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(String id);
    String createProduct(Product product);
    String updateProduct(String id, Product updateData);
    String deleteProduct(String id);
}