package com.telcox.productcatalog.api;

import com.telcox.productcatalog.service.PlanVersioningService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/plan")
public class PlanController {

    private final PlanVersioningService planVersioningService;

    public PlanController(PlanVersioningService planVersioningService) {
        this.planVersioningService = planVersioningService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(@PathVariable UUID productId, @Valid @RequestBody PlanRequest request) {
        return planVersioningService.createOrReplacePlan(productId, request);
    }

    @GetMapping
    public PlanResponse getPlan(@PathVariable UUID productId) {
        return planVersioningService.getPlan(productId);
    }

    @PostMapping("/close")
    public PlanResponse closePlan(@PathVariable UUID productId,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closingDate) {
        return planVersioningService.closePlan(productId, closingDate);
    }

    @PostMapping("/features")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanFeatureResponse addFeature(@PathVariable UUID productId, @Valid @RequestBody PlanFeatureRequest request) {
        return planVersioningService.addFeature(productId, request);
    }

    @PostMapping("/categories/{categoryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignCategory(@PathVariable UUID productId, @PathVariable UUID categoryId) {
        planVersioningService.assignCategory(productId, categoryId);
    }

    @GetMapping("/categories")
    public List<UUID> listCategories(@PathVariable UUID productId) {
        return planVersioningService.listCategoriesForProduct(productId);
    }
}
