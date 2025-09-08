package com.smartretail.backend.repository;

import com.smartretail.backend.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findByProductId(String productId);
    boolean existsByProductId(String productId);

    @Query(value = "{ 'productId' : ?0 }", delete = true)
    void deleteByProductId(String productId);
}