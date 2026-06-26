package dev.sverdlov.productservice.domain;

import dev.sverdlov.productservice.api.ProductCreateRequest;
import dev.sverdlov.productservice.api.ProductUpdateRequest;
import dev.sverdlov.productservice.domain.db.ProductEntity;

public interface ProductService {
    ProductEntity create(ProductCreateRequest createRequest);
    ProductEntity update(Long id, ProductUpdateRequest updateRequest);
    ProductEntity getById(Long id);
    void delete(Long id);
}
