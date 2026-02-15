package com.example.product.repository;

import com.example.product.entity.Product;
import com.example.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,String> {

    Optional<Product> findBySku(String sku);
    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);
    @Query(value = "SELECT * FROM products WHERE id = ?1 AND product_status IN ('ACTIVE', 'INACTIVE')", nativeQuery = true)
    Optional<Product> findByIdAndStatusActiveOrInactive(String id);
    
    @Query("SELECT p FROM Product p WHERE p.id = ?1 AND p.productStatus IN ('ACTIVE', 'INACTIVE')")
    Optional<Product> findByIdAndProductStatusActiveOrInactive(String id);
}
