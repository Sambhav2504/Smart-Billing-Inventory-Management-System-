package com.smartretail.backend.service;

import com.smartretail.backend.dto.ProductRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service // 👈 This annotation tells Spring this is a service layer component
public class ProductServiceImpl implements ProductService { // 👈 Implements the interface

    // Temporary fake database (moved from the controller)
    private List<ProductRequest> fakeProductDatabase = new ArrayList<>();

    @Override
    public List<ProductRequest> getAllProducts() {
        System.out.println("[SERVICE] Fetching all products. Count: " + fakeProductDatabase.size());
        return fakeProductDatabase;
    }

    @Override
    public ProductRequest getProductById(String id) {
        System.out.println("[SERVICE] Fetching product with ID: " + id);
        Optional<ProductRequest> foundProduct = fakeProductDatabase.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();

        return foundProduct.orElse(null); // Return the product if found, else return null
    }

    @Override
    public String createProduct(ProductRequest productRequest) {
        System.out.println("[SERVICE] Creating product: " + productRequest.getName());

        String newId = UUID.randomUUID().toString();
        ProductRequest productToSave = new ProductRequest();
        productToSave.setId(newId);
        productToSave.setProductId(productRequest.getProductId());
        productToSave.setName(productRequest.getName());
        productToSave.setCategory(productRequest.getCategory());
        productToSave.setPrice(productRequest.getPrice());
        productToSave.setQuantity(productRequest.getQuantity());

        fakeProductDatabase.add(productToSave);
        System.out.println("[SERVICE] Product created successfully. ID: " + newId);
        return "Product created successfully. ID: " + newId;
    }

    @Override
    public String updateProduct(String id, ProductRequest updateData) {
        System.out.println("[SERVICE] Updating product ID: " + id);
        for (ProductRequest product : fakeProductDatabase) {
            if (product.getId().equals(id)) {
                if (updateData.getName() != null) product.setName(updateData.getName());
                if (updateData.getProductId() != null) product.setProductId(updateData.getProductId());
                if (updateData.getCategory() != null) product.setCategory(updateData.getCategory());
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
        boolean wasRemoved = fakeProductDatabase.removeIf(product -> product.getId().equals(id));
        if (wasRemoved) {
            System.out.println("[SERVICE] Product deleted successfully.");
            return "Product deleted successfully";
        } else {
            System.out.println("[SERVICE] Delete failed. Product not found for ID: " + id);
            return "Error: Product not found";
        }
    }
}