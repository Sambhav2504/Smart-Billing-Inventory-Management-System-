package com.smartretail.backend.controller;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.service.FileService;
import com.smartretail.backend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final FileService fileService;
    public ProductController(ProductService productService, FileService fileService) {
        this.productService = productService;
        this.fileService = fileService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'CASHIER')")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // General update (partial updates supported)
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody Product updateData) {
        Product updatedProduct = productService.updateProduct(productId, updateData);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    // Single image upload endpoint (separate)
    @PutMapping("/{productId}/image")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<?> updateProductImage(@PathVariable String productId, @RequestParam("image") MultipartFile imageFile) {
        try {
            Product updateData = new Product();
            // If you want to force replacement, leave updateData.imageId empty string or null
            Product updatedProduct = productService.updateProduct(productId, updateData, imageFile);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }
    @GetMapping("/{productId}/image")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','CASHIER')")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        if (product.getImageId() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] image = fileService.getImage(product.getImageId());
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // or fetch from metadata
                .body(image);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> bulkUploadProducts(@RequestParam("file") MultipartFile csvFile) {
        try {
            List<Product> products = parseCsv(csvFile.getInputStream());
            for (Product product : products) {
                productService.addProduct(product);
            }
            return new ResponseEntity<>("Bulk upload successful: " + products.size() + " products added", HttpStatus.OK);
        } catch (IOException | CsvValidationException e) {
            return new ResponseEntity<>("Bulk upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Product> parseCsv(InputStream inputStream) throws IOException, CsvValidationException {
        List<Product> products = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] headers = reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                Product product = new Product();
                product.setProductId(line[0]);
                product.setName(line[1]);
                product.setCategory(line[2]);
                product.setPrice(Double.parseDouble(line[3]));
                product.setQuantity(Integer.parseInt(line[4]));
                product.setReorderLevel(Integer.parseInt(line[5]));
                product.setExpiryDate((line.length > 6 && line[6] != null && !line[6].isEmpty()) ? csvDateFormat.parse(line[6]) : null);
                products.add(product);
            }
        } catch (Exception e) {
            // wrap parse exceptions as IOException so controller handles uniformly
            throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
        }
        return products;
    }
}
