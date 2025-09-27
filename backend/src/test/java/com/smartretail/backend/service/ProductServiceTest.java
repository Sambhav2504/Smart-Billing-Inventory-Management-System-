package com.smartretail.backend.service;

import com.smartretail.backend.exception.ProductNotFoundException;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FileService fileService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Locale locale;

    @BeforeEach
    void setUp() {
        locale = Locale.forLanguageTag("en");

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

        // Mock MessageSource for error messages
        when(messageSource.getMessage(eq("product.exists"), any(), eq(locale)))
                .thenReturn("Product already exists: p12345678");
        when(messageSource.getMessage(eq("product.not.found"), any(), eq(locale)))
                .thenReturn("Product not found: p12345678");
        when(messageSource.getMessage(eq("product.restock.invalid.quantity"), any(), eq(locale)))
                .thenReturn("Restock quantity must be positive");
    }

    @Test
    void testAddProductSuccess() {
        when(productRepository.existsByProductId("p12345678")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.addProduct(product, locale);

        assertNotNull(result);
        assertEquals("p12345678", result.getProductId());
        verify(productRepository).save(product);
    }

    @Test
    void testAddProductDuplicateId() {
        when(productRepository.existsByProductId("p12345678")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.addProduct(product, locale));
        assertEquals("Product already exists: p12345678", exception.getMessage());
    }

    @Test
    void testGetLowStockProducts() {
        when(productRepository.findLowStockProducts()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getLowStockProducts();

        assertEquals(1, products.size());
        assertEquals("p12345678", products.get(0).getProductId());
    }

    @Test
    void testGetExpiringProducts() {
        product.setExpiryDate(new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L));
        when(productRepository.findByExpiryDateBefore(any(Date.class))).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getExpiringProducts(
                new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));

        assertEquals(1, products.size());
        assertEquals("p12345678", products.get(0).getProductId());
    }

    @Test
    void testRestockProductSuccess() {
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.restockProduct("p12345678", 5, locale);

        assertEquals(13, result.getQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void testRestockProductInvalidQty() {
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.of(product));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.restockProduct("p12345678", 0, locale));
        assertEquals("Restock quantity must be positive", exception.getMessage());
    }

    @Test
    void testRestockProductNotFound() {
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.restockProduct("p12345678", 5, locale));
        assertEquals("Product not found: p12345678", exception.getMessage());
    }

    @Test
    void testGetProductByIdSuccess() {
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.of(product));

        Product result = productService.getProductById("p12345678", locale);

        assertNotNull(result);
        assertEquals("p12345678", result.getProductId());
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findByProductId("p12345678")).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById("p12345678", locale));
        assertEquals("Product not found: p12345678", exception.getMessage());
    }
}