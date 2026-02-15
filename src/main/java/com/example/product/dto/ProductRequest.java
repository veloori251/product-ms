package com.example.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @NotBlank(message = "sku is required")
        String sku,
        @NotBlank(message = "Product name is required")
        String name,
        String description,
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be grater than 0")
        Double price,
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity cannot be negative")
        Integer availableQuantity,
        @NotBlank(message = "Categoty Id is required")
        String categoryId

) {}
