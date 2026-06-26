package dev.sverdlov.productservice.api;


import dev.sverdlov.productservice.domain.CacheMode;
import dev.sverdlov.productservice.domain.ProductService;
import dev.sverdlov.productservice.domain.db.ProductEntity;
import dev.sverdlov.productservice.domain.service.DbProductService;
import dev.sverdlov.productservice.domain.service.ManualCachingProductService;
import dev.sverdlov.productservice.domain.service.SpringAnnotationCachingProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final DbProductService dbProductService;
    private final ManualCachingProductService manualCachingProductService;
    private final SpringAnnotationCachingProductService springAnnotationCachingProductService;
    private final ProductDtoMapper mapper;

    @PostMapping
    public ResponseEntity<ProductDto> create(
            @RequestBody ProductCreateRequest request,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Creating product with cacheMode={}", cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity product = service.create(request);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(
            @PathVariable("id") Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        log.info("Getting product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);//ProductService - ManualCaching
        ProductEntity product = service.getById(id);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode

    ) {
        log.info("Updating product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        ProductEntity product = service.update(id, request);
        ProductDto dto = mapper.toProductDto(product);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode

    ) {
        log.info("Deleting product {} with cacheMode={}", id, cacheMode);

        ProductService service = resolveProductService(cacheMode);
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    private ProductService resolveProductService(CacheMode cacheMode) {
        return switch (cacheMode) {
            case NONE_CACHE -> dbProductService;
            case MANUAL -> manualCachingProductService;
            case SPRING -> springAnnotationCachingProductService;
        };
    }
}

