package com.smartretail.backend.service;

import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId("p12345678");
        product.setName("Laptop");
        product.setCategory("Electronics");
        product.setPrice(999.99);
        product.setQuantity(8);
        product.setReorderLevel(10);
        product.setExpiryDate(null);
        product.setAddedBy("ravi@shop.com");
        product.setLastUpdated(new Date());
    }

    @Test
    void testAddProductSuccess() {
        when(productRepository.existsByProductId(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.addProduct(product);

        assertNotNull(result);
        assertEquals("p12345678", result.getProductId());
        verify(productRepository).save(product);
    }

    @Test
    void testAddProductDuplicateId() {
        when(productRepository.existsByProductId("p12345678")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.addProduct(product));
        assertEquals("Product ID already exists: p12345678", exception.getMessage());
    }

    @Test
    void testGetLowStockProducts() {
        when(productRepository.findByQuantityLessThanReorderLevel()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getLowStockProducts();

        assertEquals(1, products.size());
        assertEquals("p12345678", products.get(0).getProductId());
    }

    @Test
    void testGetExpiringProducts() {
        product.setExpiryDate(new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L));
        when(productRepository.findByExpiryDateBeforeAndExpiryDateNotNull(any(Date.class))).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getExpiringProducts(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));

        assertEquals(1, products.size());
        assertEquals("p12345678", products.get(0).getProductId());
    }

    @Test
    void testRestockProductSuccess() {
        when(productRepository.findByProductId("p12345678")).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.restockProduct("p12345678", 5);

        assertEquals(13, result.getQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void testRestockProductInvalidQty() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.restockProduct("p12345678", 0));
        assertEquals("Restock quantity must be positive", exception.getMessage());
    }

    @Test
    void testRestockProductNotFound() {
        when(productRepository.findByProductId("p12345678")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.restockProduct("p12345678", 5));
        assertEquals("Product not found: p12345678", exception.getMessage());
    }
}