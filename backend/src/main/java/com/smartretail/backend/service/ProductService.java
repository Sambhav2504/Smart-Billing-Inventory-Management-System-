package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ProductService {
    Product addProduct(Product product);
    Product getProductById(String productId);
    List<Product> getAllProducts();
    Product updateProduct(String productId, Product product);
    Product updateProduct(String productId, Product product, MultipartFile imageFile) throws IOException;
    void deleteProduct(String productId);
    List<Product> getLowStockProducts();
    List<Product> getExpiringProducts(Date threshold);
    Product restockProduct(String productId, int restockQty);
}
