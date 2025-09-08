package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    private List<Product> productDatabase = new ArrayList<>();

    @Override
    public List<Product> getAllProducts() {
        System.out.println("[SERVICE] Fetching all products. Count: " + productDatabase.size());
        return productDatabase;
    }

    @Override
    public Product getProductById(String id) {
        System.out.println("[SERVICE] Fetching product with ID: " + id);
        Optional<Product> foundProduct = productDatabase.stream()
                .filter(product -> product.getProductId().equals(id))
                .findFirst();
        return foundProduct.orElse(null);
    }

    @Override
    public String createProduct(Product product) {
        System.out.println("[SERVICE] Creating product: " + product.getName());
        String newId = "p" + UUID.randomUUID().toString().substring(0, 8);
        product.setProductId(newId);
        productDatabase.add(product);
        System.out.println("[SERVICE] Product created successfully. ID: " + newId);
        return "Product created successfully. ID: " + newId;
    }

    @Override
    public String updateProduct(String id, Product updateData) {
        System.out.println("[SERVICE] Updating product ID: " + id);
        for (Product product : productDatabase) {
            if (product.getProductId().equals(id)) {
                if (updateData.getName() != null) product.setName(updateData.getName());
                if (updateData.getDescription() != null) product.setDescription(updateData.getDescription());
                if (updateData.getPrice() != 0) product.setPrice(updateData.getPrice());
                if (updateData.getQuantity() != 0) product.setQuantity(updateData.getQuantity());
                System.out.println("[SERVICE] Product updated successfully: " + product.getName());
                return "Product updated successfully";
            }
        }
        System.out.println("[SERVICE] Update failed. Product not found for ID: " + id);
        return "Error: Product not found";
    }

    @Override
    public String deleteProduct(String id) {
        System.out.println("[SERVICE] Deleting product ID: " + id);
        boolean wasRemoved = productDatabase.removeIf(product -> product.getProductId().equals(id));
        if (wasRemoved) {
            System.out.println("[SERVICE] Product deleted successfully.");
            return "Product deleted successfully";
        } else {
            System.out.println("[SERVICE] Delete failed. Product not found for ID: " + id);
            return "Error: Product not found";
        }
    }
}