package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.CategoryRequest;
import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.CategoryMapper;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.CategoryService;
import com.nexjob.platform.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toResponse).toList();
    }

    @Override
    public Page<CategoryResponse> adminList(String q, Pageable pageable) {
        Page<Category> page = (q == null || q.isBlank())
                ? categoryRepository.findByIsActiveTrue(pageable)
                : categoryRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q.trim(), pageable);
        return page.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = uniqueSlug(request.getName(), null);
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findActive(id);
        if (!category.getName().equalsIgnoreCase(request.getName())) {
            category.setSlug(uniqueSlug(request.getName(), id));
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Category category = findActive(id);
        category.setIsActive(false);
        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        categoryRepository.save(category);
    }

    private Category findActive(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new ResourceNotFoundException("Categoria no encontrada: " + id);
        }
        return category;
    }

    private String uniqueSlug(String name, Long excludeId) {
        String base = SlugUtils.slugify(name);
        String candidate = base;
        int suffix = 2;
        while (isSlugTaken(candidate, excludeId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean isSlugTaken(String slug, Long excludeId) {
        return categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(excludeId))
                .isPresent();
    }
}
