package com.synkrotech.mvp.products;

import java.util.List;
import java.util.UUID;

import com.synkrotech.mvp.common.BusinessException;
import com.synkrotech.mvp.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<Category> list(Boolean onlyActive) {
        return Boolean.TRUE.equals(onlyActive)
                ? categories.findByActiveTrueOrderByNameAsc()
                : categories.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category getById(UUID id) {
        return categories.findById(id)
                .orElseThrow(() -> new NotFoundException("No category exists with id " + id + "."));
    }

    @Transactional
    public Category create(CategoryRequest request) {
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCaseAndActiveTrue(name)) {
            throw new BusinessException("An active category named \"" + name + "\" already exists.");
        }
        return categories.save(new Category(name));
    }
}
