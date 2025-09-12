package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;

import java.util.Date;
import java.util.List;

public interface ProductService {
    Product addProduct(Product product);
    Product getProductById(String productId);
    List<Product> getAllProducts();
    Product updateProduct(String productId, Product product);
    void deleteProduct(String productId);
    List<Product> getLowStockProducts();
    List<Product> getExpiringProducts(Date threshold);
    Product restockProduct(String productId, int restockQty);
}