package com.example.cruddemo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductUpdateRequest(
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be greater than or equal to 0.00")
    @Digits(integer = 10, fraction = 2, message = "Price format must be up to 10 integer digits and 2 decimal places")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    Integer stockQuantity
) {}
