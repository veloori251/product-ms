package com.example.product.service;

import com.example.product.dto.ApiResponse;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.dto.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Response<ProductResponse> createProduct(ProductRequest productRequest);
    Response<ProductResponse> getProductById(String id);
    Response<List<ProductResponse>> getProductAllProducts(int page, int size, String sortBy, String direction);
    void deleteProduct(String id);
    Response<ProductResponse> updateProduct(String id,ProductRequest request);
}
