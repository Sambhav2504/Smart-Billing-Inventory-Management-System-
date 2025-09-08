package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(String productId);
    Product createProduct(Product product);
    Product updateProduct(String productId, Product updateData);
    void deleteProduct(String productId);
}