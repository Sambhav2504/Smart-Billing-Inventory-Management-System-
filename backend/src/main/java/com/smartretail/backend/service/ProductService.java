package com.smartretail.backend.service;

import com.smartretail.backend.dto.ProductRequest;
import java.util.List;

// This interface defines the contract for all product-related operations.
public interface ProductService {
    List<ProductRequest> getAllProducts();
    ProductRequest getProductById(String id);
    String createProduct(ProductRequest productRequest);
    String updateProduct(String id, ProductRequest updateData);
    String deleteProduct(String id);
}