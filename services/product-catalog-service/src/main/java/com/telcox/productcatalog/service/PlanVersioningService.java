package com.telcox.productcatalog.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.productcatalog.api.CategoryRequest;
import com.telcox.productcatalog.api.CategoryResponse;
import com.telcox.productcatalog.api.PlanFeatureRequest;
import com.telcox.productcatalog.api.PlanFeatureResponse;
import com.telcox.productcatalog.api.PlanRequest;
import com.telcox.productcatalog.api.PlanResponse;
import com.telcox.productcatalog.domain.Category;
import com.telcox.productcatalog.domain.Plan;
import com.telcox.productcatalog.domain.PlanFeature;
import com.telcox.productcatalog.domain.Product;
import com.telcox.productcatalog.domain.ProductCategory;
import com.telcox.productcatalog.repository.CategoryRepository;
import com.telcox.productcatalog.repository.PlanFeatureRepository;
import com.telcox.productcatalog.repository.PlanRepository;
import com.telcox.productcatalog.repository.ProductCategoryRepository;
import com.telcox.productcatalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * CAT-02 / FR-07/08: Product tipinin PLAN oldugu urunler icin tarife detayi,
 * kota (feature) ve kategori yonetimi.
 *
 * VERSIONING NOTU (bkz. Plan.java yorumu): db.sql semasinda PRODUCT_SERVICE_PLAN.productId
 * UNIQUE oldugu icin bir Product'in birden fazla Plan satiri olamaz. Bu yuzden
 * "tarife versiyonlama" burada Product seviyesinde ele alinir: yeni bir versiyon
 * cikarmak istediginizde ONCE ProductCatalogService.createProduct(...) ile yeni bir
 * kod altinda (ör. "PLAN_4G_V2") yeni Product yaratilir, SONRA bu servisteki
 * createOrReplacePlan(...) ile o yeni Product'a Plan atanir. Eski Product ise
 * ProductCatalogService.retireProduct(...) ile RETIRED yapilir. Bu tasarim
 * karari hoca/ekip ile teyit edilmelidir; alternatif olarak semaya
 * "productId + validFrom" bilesik anahtari eklenerek gercek versiyon satirlari
 * da tutulabilir.
 */
@Service
public class PlanVersioningService {

    private final ProductRepository productRepository;
    private final PlanRepository planRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public PlanVersioningService(ProductRepository productRepository,
                                 PlanRepository planRepository,
                                 PlanFeatureRepository planFeatureRepository,
                                 CategoryRepository categoryRepository,
                                 ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.planRepository = planRepository;
        this.planFeatureRepository = planFeatureRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    // ---- Plan ----

    @Transactional
    public PlanResponse createOrReplacePlan(UUID productId, PlanRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found: " + productId));

        if (planRepository.findByProductId(productId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Product already has a plan. Retire the old product and create a new product/plan version instead: " + productId);
        }

        LocalDate effectiveFrom = request.validFrom() == null ? LocalDate.now() : request.validFrom();
        Plan plan = new Plan(product.getId(), request.planType(), request.commitmentMonths(),
                request.monthlyPrice(), request.currency(), effectiveFrom);
        plan = planRepository.save(plan);

        List<PlanFeatureResponse> featureResponses = List.of();
        if (request.features() != null && !request.features().isEmpty()) {
            UUID planId = plan.getId();
            featureResponses = request.features().stream()
                    .map(f -> addFeatureInternal(planId, f))
                    .toList();
        }

        return PlanResponse.from(plan, featureResponses);
    }

    public PlanResponse getPlan(UUID productId) {
        Plan plan = planRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Plan not found for product: " + productId));
        List<PlanFeatureResponse> features = planFeatureRepository.findByPlanId(plan.getId()).stream()
                .map(PlanFeatureResponse::from)
                .toList();
        return PlanResponse.from(plan, features);
    }

    /** CAT-02: Bu tarife versiyonunu belirtilen tarihte kapatir (yeni Product/Plan'a gecis oncesi). */
    @Transactional
    public PlanResponse closePlan(UUID productId, LocalDate closingDate) {
        Plan plan = planRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Plan not found for product: " + productId));
        plan.closeAt(closingDate == null ? LocalDate.now() : closingDate);
        List<PlanFeatureResponse> features = planFeatureRepository.findByPlanId(plan.getId()).stream()
                .map(PlanFeatureResponse::from)
                .toList();
        return PlanResponse.from(plan, features);
    }

    // ---- Plan feature ----

    @Transactional
    public PlanFeatureResponse addFeature(UUID productId, PlanFeatureRequest request) {
        Plan plan = planRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Plan not found for product: " + productId));
        return addFeatureInternal(plan.getId(), request);
    }

    private PlanFeatureResponse addFeatureInternal(UUID planId, PlanFeatureRequest request) {
        PlanFeature feature = new PlanFeature(planId, request.featureType(), request.allowance(), request.unit(), request.isUnlimited());
        return PlanFeatureResponse.from(planFeatureRepository.save(feature));
    }

    // ---- Category (FR-08) ----

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category(request.code(), request.name(), request.parentCategoryId());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> listRootCategories() {
        return categoryRepository.findByParentCategoryIdIsNull().stream().map(CategoryResponse::from).toList();
    }

    public List<CategoryResponse> listChildCategories(UUID parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId).stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public void assignCategory(UUID productId, UUID categoryId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found: " + productId));
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found: " + categoryId));
        productCategoryRepository.save(ProductCategory.of(productId, categoryId));
    }

    public List<UUID> listCategoriesForProduct(UUID productId) {
        return productCategoryRepository.findById_ProductId(productId).stream()
                .map(pc -> pc.getId().getCategoryId())
                .toList();
    }
}
