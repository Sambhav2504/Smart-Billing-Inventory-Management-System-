package com.smartretail.backend.repository;
import com.smartretail.backend.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findByProductId(String productId);
    boolean existsByProductId(String productId);
    List<Product> findByExpiryDateBefore(Date threshold);
    @Query("{ $expr: { $lt: ['$quantity', '$reorderLevel'] } }")
    List<Product> findLowStockProducts();
}
