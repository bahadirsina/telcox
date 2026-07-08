package com.telcox.productcatalog.api;

import com.telcox.productcatalog.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request,
                                  @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return productCatalogService.createProduct(request, correlationId);
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productCatalogService.listProducts();
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable UUID productId) {
        return productCatalogService.getProduct(productId);
    }

    @PostMapping("/{productId}/activate")
    public ProductResponse activate(@PathVariable UUID productId) {
        return productCatalogService.activateProduct(productId);
    }

    @PostMapping("/{productId}/retire")
    public ProductResponse retire(@PathVariable UUID productId) {
        return productCatalogService.retireProduct(productId);
    }

    @PostMapping("/{productId}/prices")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductPriceResponse setPrice(@PathVariable UUID productId, @Valid @RequestBody ProductPriceRequest request) {
        return productCatalogService.setPrice(productId, request);
    }

    @GetMapping("/{productId}/prices")
    public List<ProductPriceResponse> listPrices(@PathVariable UUID productId) {
        return productCatalogService.listPrices(productId);
    }

    @GetMapping("/{productId}/prices/effective")
    public ProductPriceResponse getEffectivePrice(@PathVariable UUID productId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
        return productCatalogService.getEffectivePrice(productId, onDate);
    }
}
