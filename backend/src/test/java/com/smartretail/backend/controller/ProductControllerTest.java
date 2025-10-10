package com.smartretail.backend.controller;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void testCreateProduct_Success() throws IOException {
        // Arrange
        String productJson = """
                {
                    "productId": "p123",
                    "name": "Product A",
                    "category": "Electronics",
                    "price": 299.99,
                    "quantity": 10
                }
                """;

        MultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "test".getBytes());

        Product mockProduct = new Product();
        mockProduct.setProductId("p123");
        mockProduct.setName("Product A");
        mockProduct.setPrice(299.99);
        mockProduct.setQuantity(10);

        when(productService.createProduct(any(Product.class), any(MultipartFile.class), any(Locale.class)))
                .thenReturn(mockProduct);

        // Act
        ResponseEntity<Map<String, String>> response = productController.createProduct(productJson, imageFile, Locale.ENGLISH);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Product added successfully", response.getBody().get("message"));
        assertEquals("p123", response.getBody().get("productId"));
        verify(productService, times(1)).createProduct(any(Product.class), any(MultipartFile.class), any(Locale.class));
    }

    @Test
    void testCreateProduct_NoImage_Success() throws IOException {
        // Arrange
        String productJson = """
                {
                    "productId": "p123",
                    "name": "Product A",
                    "category": "Electronics", 
                    "price": 299.99,
                    "quantity": 10
                }
                """;

        Product mockProduct = new Product();
        mockProduct.setProductId("p123");
        mockProduct.setName("Product A");
        mockProduct.setPrice(299.99);
        mockProduct.setQuantity(10);

        when(productService.createProduct(any(Product.class), isNull(), any(Locale.class)))
                .thenReturn(mockProduct);

        // Act
        ResponseEntity<Map<String, String>> response = productController.createProduct(productJson, null, Locale.ENGLISH);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Product added successfully", response.getBody().get("message"));
        assertEquals("p123", response.getBody().get("productId"));
        verify(productService, times(1)).createProduct(any(Product.class), isNull(), any(Locale.class));
    }
}