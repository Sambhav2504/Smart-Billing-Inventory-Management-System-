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
        Product product = new Product();
        product.setProductId("p123");
        product.setName("Product A");
        product.setPrice(299.99);
        product.setQuantity(10);
        MultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "test".getBytes());

        when(productService.createProduct(any(Product.class), any(MultipartFile.class), any(Locale.class)))
                .thenReturn(product);

        // Act
        ResponseEntity<Product> response = productController.createProduct(product, imageFile, Locale.ENGLISH);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Should be 201 CREATED
        assertEquals("p123", response.getBody().getProductId());
        verify(productService, times(1)).createProduct(any(Product.class), any(MultipartFile.class), any(Locale.class));
    }

    @Test
    void testCreateProduct_NoImage_Success() throws IOException {
        // Arrange
        Product product = new Product();
        product.setProductId("p123");
        product.setName("Product A");
        product.setPrice(299.99);
        product.setQuantity(10);

        when(productService.createProduct(any(Product.class), isNull(), any(Locale.class)))
                .thenReturn(product);

        // Act
        ResponseEntity<Product> response = productController.createProduct(product, null, Locale.ENGLISH);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Should be 201 CREATED
        assertEquals("p123", response.getBody().getProductId());
        verify(productService, times(1)).createProduct(any(Product.class), isNull(), any(Locale.class));
    }

    @Test
    void testCreateProduct_ServiceThrowsException() throws IOException {
        // Arrange
        Product product = new Product();
        product.setProductId("p123");
        MultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "test".getBytes());

        when(productService.createProduct(any(Product.class), any(MultipartFile.class), any(Locale.class)))
                .thenThrow(new IOException("File processing failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            productController.createProduct(product, imageFile, Locale.ENGLISH);
        });
    }
}