package com.telcox.productcatalog.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.productcatalog.api.ProductPriceRequest;
import com.telcox.productcatalog.api.ProductPriceResponse;
import com.telcox.productcatalog.api.ProductRequest;
import com.telcox.productcatalog.api.ProductResponse;
import com.telcox.productcatalog.domain.Product;
import com.telcox.productcatalog.domain.ProductOutboxEvent;
import com.telcox.productcatalog.domain.ProductPrice;
import com.telcox.productcatalog.repository.ProductOutboxEventRepository;
import com.telcox.productcatalog.repository.ProductPriceRepository;
import com.telcox.productcatalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductCatalogService {

    private static final String AGGREGATE_TYPE = "PRODUCT";

    private final ProductRepository productRepository;
    private final ProductPriceRepository priceRepository;
    private final ProductOutboxEventRepository outboxEventRepository;

    public ProductCatalogService(ProductRepository productRepository,
                                 ProductPriceRepository priceRepository,
                                 ProductOutboxEventRepository outboxEventRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    // ---- Product (FR-05) ----

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String correlationId) {
        if (productRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.CONFLICT, "A product with this code already exists: " + request.code());
        }
        Product product = Product.create(request.code(), request.name(), request.description(), request.productType());
        product = productRepository.save(product);
        publishEvent(product.getId(), "ProductCreated", Map.of("code", product.getCode(), "productType", product.getProductType().name()), correlationId);
        return ProductResponse.from(product);
    }

    public List<ProductResponse> listProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getProduct(UUID productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Transactional
    public ProductResponse activateProduct(UUID productId) {
        Product product = findProduct(productId);
        product.activate();
        publishEvent(product.getId(), "ProductActivated", Map.of("status", product.getStatus().name()), null);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse retireProduct(UUID productId) {
        Product product = findProduct(productId);
        product.retire();
        publishEvent(product.getId(), "ProductRetired", Map.of("status", product.getStatus().name()), null);
        return ProductResponse.from(product);
    }

    // ---- Price / effective-date (FR-06) ----

    /**
     * FR-06: Yeni fiyat ekler. Su an acik olan (validTo = null) fiyat kaydi varsa,
     * yeni fiyatin gecerlilik gununden bir gun oncesinde kapatilir - boylece ayni anda
     * sadece bir gecerli fiyat olur.
     */
    @Transactional
    public ProductPriceResponse setPrice(UUID productId, ProductPriceRequest request) {
        findProduct(productId); // var olma kontrolu
        LocalDate effectiveFrom = request.validFrom() == null ? LocalDate.now() : request.validFrom();

        priceRepository.findByProductIdAndValidToIsNull(productId)
                .ifPresent(current -> current.closeAt(effectiveFrom.minusDays(1)));

        ProductPrice newPrice = new ProductPrice(productId, request.price(), request.currency(), request.taxIncluded(), effectiveFrom);
        newPrice = priceRepository.save(newPrice);

        publishEvent(productId, "ProductPriceChanged",
                Map.of("price", newPrice.getPrice().toString(), "currency", newPrice.getCurrency(), "validFrom", effectiveFrom.toString()),
                null);

        return ProductPriceResponse.from(newPrice);
    }

    public List<ProductPriceResponse> listPrices(UUID productId) {
        findProduct(productId);
        return priceRepository.findByProductIdOrderByValidFromDesc(productId).stream().map(ProductPriceResponse::from).toList();
    }

    /** FR-06: Belirli bir tarihte gecerli olan fiyati doner (varsayilan: bugun). */
    public ProductPriceResponse getEffectivePrice(UUID productId, LocalDate onDate) {
        findProduct(productId);
        LocalDate date = onDate == null ? LocalDate.now() : onDate;
        return priceRepository.findByProductIdOrderByValidFromDesc(productId).stream()
                .filter(p -> p.isEffectiveOn(date))
                .findFirst()
                .map(ProductPriceResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "No effective price found for product " + productId + " on " + date));
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found: " + productId));
    }

    private void publishEvent(UUID aggregateId, String eventType, Map<String, Object> payloadExtra, String correlationId) {
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? "product-" + UUID.randomUUID()
                : correlationId;
        Map<String, Object> payload = new LinkedHashMap<>(payloadExtra);
        payload.put("productId", aggregateId.toString());
        ProductOutboxEvent event = new ProductOutboxEvent(aggregateId, AGGREGATE_TYPE, eventType, payload, resolvedCorrelationId);
        outboxEventRepository.save(event);
    }
}
