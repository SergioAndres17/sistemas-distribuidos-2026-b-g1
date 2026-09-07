package com.synkrotech.mvp.products;

import java.util.List;
import java.util.UUID;

import com.synkrotech.mvp.common.BusinessException;
import com.synkrotech.mvp.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<Product> list(Boolean onlyActive) {
        return Boolean.TRUE.equals(onlyActive)
                ? products.findActiveWithCategory()
                : products.findAllWithCategory();
    }

    /** Used by the sales flow, which needs the entity itself. */
    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return products.findByIdWithCategory(id)
                .orElseThrow(() -> new NotFoundException("No product exists with id " + id + "."));
    }

    @Transactional
    public Product create(ProductRequest request) {
        Category category = resolveActiveCategory(request.categoryId());
        return products.save(new Product(
                request.name().trim(),
                request.price(),
                request.stock(),
                category));
    }

    @Transactional
    public Product update(UUID id, ProductRequest request) {
        Product product = getById(id);
        Category category = resolveActiveCategory(request.categoryId());
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);
        return product;
    }

    /** Soft delete, so sale details still resolve to a real product. */
    @Transactional
    public Product deactivate(UUID id) {
        Product product = getById(id);
        product.setActive(false);
        return product;
    }

    /**
     * A product may only hang off a category that exists and is still in use —
     * otherwise the catalogue would grow entries nobody can find.
     */
    private Category resolveActiveCategory(UUID categoryId) {
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new BusinessException("No category exists with id " + categoryId + "."));
        if (!category.isActive()) {
            throw new BusinessException("The category " + category.getName() + " is deactivated.");
        }
        return category;
    }
}
