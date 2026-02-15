package com.example.product.controller;

import com.example.product.dto.ApiResponse;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.dto.Response;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {


    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Validated @RequestBody ProductRequest request){
        log.info("Product received with sku: {} ",request.sku());
        Response<ProductResponse> response = productService.createProduct(request);
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                response.content(),
                response.metadata(),
                "Product created successfully",
                HttpStatus.CREATED.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("Fetch products request received. Page: {}, Size: {}, SortBy: {}, direction:{} ",page,size,sortBy,direction);
        Response<List<ProductResponse>> productResponses = productService.getProductAllProducts(page,size,sortBy,direction);
        ApiResponse<List<ProductResponse>> response = new ApiResponse<>(
                productResponses.content(),
                productResponses.metadata(),
                "all available products fetched",
                200,
                LocalDateTime.now()
        );
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id){
        log.info("Fetch product request received for id: {} ",id);
        Response<ProductResponse> productResponse = productService.getProductById(id);
        ApiResponse<ProductResponse> response = new ApiResponse<>(
                productResponse.content(),
                productResponse.metadata(),
                "product by given id",
                200,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id){
        log.info("Delete request received for id: {} ",id);
        productService.deleteProduct(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                null,
                null,
                "Product deleted successfully",
                204,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiResponse,HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable String id
            , @RequestBody ProductRequest request){
        log.info("Update request received for id: {}",id);
        Response<ProductResponse> productResponse = productService.updateProduct(id,request);
        ApiResponse<ProductResponse> response = new ApiResponse<>(
                productResponse.content(),
                productResponse.metadata(),
                "Product updated successfully",
                200,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
