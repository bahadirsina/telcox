package com.telcox.productcatalog.api;

import com.telcox.productcatalog.service.PlanVersioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final PlanVersioningService planVersioningService;

    public CategoryController(PlanVersioningService planVersioningService) {
        this.planVersioningService = planVersioningService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return planVersioningService.createCategory(request);
    }

    @GetMapping
    public List<CategoryResponse> listRoots() {
        return planVersioningService.listRootCategories();
    }

    @GetMapping("/{categoryId}/children")
    public List<CategoryResponse> listChildren(@PathVariable UUID categoryId) {
        return planVersioningService.listChildCategories(categoryId);
    }
}
