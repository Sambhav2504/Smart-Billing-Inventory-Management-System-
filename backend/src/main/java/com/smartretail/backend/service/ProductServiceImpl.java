package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final FileService fileService;

    public ProductServiceImpl(ProductRepository productRepository,
                              NotificationService notificationService,
                              FileService fileService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.fileService = fileService;
    }

    @Override
    @Transactional
    public Product addProduct(Product product) {
        System.out.println("[SERVICE] Adding product: " + product.getName());
        if (productRepository.existsByProductId(product.getProductId())) {
            System.out.println("[SERVICE] Add failed: Product ID already exists: " + product.getProductId());
            throw new RuntimeException("Product ID already exists");
        }
        product.setLastUpdated(new Date());
        Product savedProduct = productRepository.save(product);
        System.out.println("[SERVICE] Product added successfully: " + savedProduct.getProductId());
        return savedProduct;
    }

    @Override
    public Product updateProduct(String productId, Product product) {
        try {
            return updateProduct(productId, product, null);
        } catch (IOException e) {
            // should not happen because imageFile is null; wrap as runtime
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public Product updateProduct(String productId, Product product, MultipartFile imageFile) throws IOException {
        Optional<Product> existingProductOpt = productRepository.findByProductId(productId);

        if (existingProductOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }

        Product existingProduct = existingProductOpt.get();

        // Update fields if provided in the request
        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getCategory() != null) {
            existingProduct.setCategory(product.getCategory());
        }
        if (product.getPrice() > 0) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getQuantity() >= 0) {
            existingProduct.setQuantity(product.getQuantity());
            // low-stock check after manual update
            if (existingProduct.getQuantity() < existingProduct.getReorderLevel()) {
                notificationService.sendLowStockNotification(
                        "manager@shop.com",
                        existingProduct.getName(),
                        existingProduct.getQuantity(),
                        existingProduct.getReorderLevel()
                );
            }
        }
        if (product.getReorderLevel() >= 0) {
            existingProduct.setReorderLevel(product.getReorderLevel());
        }
        if (product.getExpiryDate() != null) {
            existingProduct.setExpiryDate(product.getExpiryDate());
        }

        // Handle image upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageId = fileService.uploadImage(imageFile, "product_" + productId);
            existingProduct.setImageId(imageId);
        } else if (product.getImageId() != null) {
            // If client intentionally sets imageId to empty string -> remove image
            if (product.getImageId().isEmpty()) {
                existingProduct.setImageId(null);
            } else {
                // if non-empty imageId provided in body, set it (rare case)
                existingProduct.setImageId(product.getImageId());
            }
        }

        existingProduct.setLastUpdated(new Date());

        return productRepository.save(existingProduct);
    }

    @Override
    public Product getProductById(String productId) {
        System.out.println("[SERVICE] Fetching product with ID: " + productId);
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
    }

    @Override
    public List<Product> getAllProducts() {
        System.out.println("[SERVICE] Fetching all products.");
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        System.out.println("[SERVICE] Deleting product ID: " + productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Delete failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        productRepository.deleteById(product.getProductId());
        System.out.println("[SERVICE] Product deleted successfully.");
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional
    public Product restockProduct(String productId, int restockQty) {
        System.out.println("[SERVICE] Restocking product ID: " + productId + " with quantity: " + restockQty);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    System.out.println("[SERVICE] Restock failed: Product not found for ID: " + productId);
                    return new RuntimeException("Product not found");
                });
        product.setQuantity(product.getQuantity() + restockQty);
        product.setLastUpdated(new Date());
        Product updatedProduct = productRepository.save(product);
        // Check low stock after restock
        if (updatedProduct.getQuantity() < updatedProduct.getReorderLevel()) {
            notificationService.sendLowStockNotification(
                    "manager@shop.com",
                    updatedProduct.getName(),
                    updatedProduct.getQuantity(),
                    updatedProduct.getReorderLevel()
            );
        }
        System.out.println("[SERVICE] Product restocked successfully: " + updatedProduct.getProductId());
        return updatedProduct;
    }

    @Override
    public List<Product> getExpiringProducts(Date threshold) {
        return productRepository.findByExpiryDateBefore(threshold).stream()
                .filter(product -> product.getExpiryDate() != null)
                .collect(Collectors.toList());
    }
}
