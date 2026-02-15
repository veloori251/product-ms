package com.example.product.service;

import com.example.product.dto.*;
import com.example.product.entity.Product;
import com.example.product.enums.ProductStatus;
import com.example.product.enums.StockStatus;
import com.example.product.exception.DuplicateResourceException;
import com.example.product.exception.FieldValidationError;
import com.example.product.exception.InvalidRequestException;
import com.example.product.exception.ResourceNotFoundException;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Response<ProductResponse> createProduct(ProductRequest productRequest) {
        log.info("Creating product with sku: {}",productRequest.sku());
        if(productRepository.findBySku(productRequest.sku()).isPresent()){
            throw new DuplicateResourceException("sku already exists");
        }
        Product newProduct = new Product();
        newProduct.setSku(productRequest.sku());
        newProduct.setName(productRequest.name());
        newProduct.setDescription(productRequest.description());
        newProduct.setPrice(productRequest.price());
        newProduct.setAvailableQuantity(productRequest.availableQuantity());
        newProduct.setCategoryId(productRequest.categoryId());
        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setUpdatedAt(LocalDateTime.now());
        newProduct.setProductStatus(ProductStatus.ACTIVE);
        if(productRequest.availableQuantity()>10){
            newProduct.setStockStatus(StockStatus.IN_STOCK);
        }else if(productRequest.availableQuantity()>0){
            newProduct.setStockStatus(StockStatus.LOW_STOCK);
        }else{
            newProduct.setStockStatus(StockStatus.OUT_OF_STOCK);
        }
        Product product = productRepository.save(newProduct);
        log.info("Product created successfully with id: {} ",product.getId());
        return new Response<>(mapToResponse(product),null);
    }

    @Override
    @Cacheable(value = "products",key = "#id")
    public Response<ProductResponse> getProductById(String id) {
        log.info("Fetching product with id {} ",id);
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found"));
        log.info("Product found: {} ",product.getName());
        return new Response<>(mapToResponse(product),null);
    }

    @Override
    public Response<List<ProductResponse>> getProductAllProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending()
                :Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,size,sort);
        Page<Product> productPage = productRepository.findByProductStatus(ProductStatus.ACTIVE,pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
        PaginationMetadata metadata = new PaginationMetadata(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast()
        );
        return new Response<>(content,metadata);
    }

    @Override
    @CacheEvict(value = "products",key = "#id")
    public void deleteProduct(String id) {
        log.info("Archiving product with id: {} ",id);
        Product product = productRepository.findByIdAndProductStatusActiveOrInactive(id)
                .orElseThrow(()->{
                    log.warn("Product not found for id: {}",id);
                    throw new ResourceNotFoundException("Product not found");
                });
        product.setProductStatus(ProductStatus.ARCHIVED);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("Product archived successfully with id: {} ",id);
    }
    @CachePut(value = "products",key="#id")
    public Response<ProductResponse> updateProduct(String id,ProductRequest request){
        log.info("Updating product with id: {} ",id);
        List<FieldValidationError> errors = new ArrayList<>();
        Product product = productRepository.findByIdAndProductStatusActiveOrInactive(id)
                .orElseThrow(() ->{
                    log.warn("Product not found for id: {}",id);
                    throw new ResourceNotFoundException("Product not found");
                });
        updateBasicDetails(product,request,errors);
        updateInventoryDetails(product,request,errors);
        updatePricingDetails(product,request,errors);
        if(product.getAvailableQuantity()>10)
            product.setStockStatus(StockStatus.IN_STOCK);
        else if(product.getAvailableQuantity()>0)
            product.setStockStatus(StockStatus.LOW_STOCK);
        else
            product.setStockStatus(StockStatus.OUT_OF_STOCK);
        if(errors.isEmpty()) {
            log.info("Product updated successfully with id: {}", product.getId());
            productRepository.save(product);
        }else {
            log.warn("Validation failed for product update with id: {}, Errors are : {}",id,errors);
            throw new InvalidRequestException("Validation failed", errors);
        }
        return new Response<>(mapToResponse(product),null);

    }

    private void updateBasicDetails(Product product,ProductRequest request,List<FieldValidationError> errors){
        if(request.name()!=null){
            if(request.name().isBlank()) {
                errors.add(new FieldValidationError("name", "name cannot be blank"));
            }else {
                log.info("Updating name of the product id: {} ",product.getId());
                product.setName(request.name().trim());
            }
        }
        if(request.description()!=null){
            if(request.description().isBlank()) {
                errors.add(new FieldValidationError("description", "description can not be blank"));
            }else {
                log.info("Updating description of the product id {} ",product.getId());
                product.setDescription(request.description().trim());
            }
        }
    }

    private void updatePricingDetails(Product product,ProductRequest request,List<FieldValidationError> errors){
        if(request.price()!=null){
            if(request.price()<=0)
                errors.add(new FieldValidationError("price", "price must be grater than 0"));
            else {
                log.info("Updating price for product id: {}",product.getId());
                product.setPrice(request.price());
            }
        }
    }

    private void updateInventoryDetails(Product product,ProductRequest request,List<FieldValidationError> errors){
        if(request.availableQuantity()!=null){
            if(request.availableQuantity()<0)
                errors.add(new FieldValidationError("availableQuantity", "quantity cannot be negative"));
            else {
                log.info("Updating available quantity for product id: {}",product.getId());
                product.setAvailableQuantity(request.availableQuantity());
            }
        }
    }

    private ProductResponse mapToResponse(Product product){
        ProductResponse productResponse = new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getAvailableQuantity(),
                product.getStockStatus(),
                product.getProductStatus(),
                product.getCategoryId(),
                product.getCreatedAt()
        );
        return  productResponse;
    }
}
