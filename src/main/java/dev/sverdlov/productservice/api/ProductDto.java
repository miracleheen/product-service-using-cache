package dev.sverdlov.productservice.api;


import dev.sverdlov.productservice.domain.db.ProductEntity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for {@link ProductEntity}
 */
public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}