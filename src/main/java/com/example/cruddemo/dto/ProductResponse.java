package com.example.cruddemo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
