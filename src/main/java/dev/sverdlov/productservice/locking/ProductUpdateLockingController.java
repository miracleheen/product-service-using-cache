package dev.sverdlov.productservice.locking;

import dev.sverdlov.productservice.api.ProductDto;
import dev.sverdlov.productservice.api.ProductDtoMapper;
import dev.sverdlov.productservice.api.ProductUpdateRequest;
import dev.sverdlov.productservice.domain.db.ProductEntity;
import dev.sverdlov.productservice.domain.service.DbProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/products/lock")
public class ProductUpdateLockingController {
    private final RedisLockManager redisLockManager;
    private final DbProductService dbProductService;
    private final ProductDtoMapper productDtoMapper;

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(value = "workMs", defaultValue = "500") long workMs
    ) {
        log.info("Updating product with locking: id={}", id);
        String lockKey = "product:" + id;

        var lockId = redisLockManager.tryLock(lockKey, Duration.ofMinutes(1));
        if (lockId == null) {
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Блокировка захвачена для объекта %s. Попробуйте позже".formatted(lockKey)
            );
        }

        try {
            try {
                Thread.sleep(workMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            ProductEntity product = dbProductService.update(id, request);
            ProductDto dto = productDtoMapper.toProductDto(product);
            log.info("Product has been updated: id={}", id);
            return ResponseEntity.ok(dto);
        } finally {
            redisLockManager.unlockLock(lockKey, lockId);
        }
    }
}
