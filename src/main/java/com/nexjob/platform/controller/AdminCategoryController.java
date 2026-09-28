package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.CategoryIntakeFieldRequest;
import com.nexjob.platform.dto.request.CategoryRequest;
import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = categoryService.adminList(q, PageRequest.of(page, size, Sort.by("name")));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getById(id));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.create(request), "Categoria creada");
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.ok(categoryService.update(id, request), "Categoria actualizada");
    }

    @PutMapping("/{id}/intake-fields")
    public ApiResponse<CategoryResponse> updateIntakeFields(@PathVariable Long id, @Valid @RequestBody List<CategoryIntakeFieldRequest> fields) {
        return ApiResponse.ok(categoryService.updateIntakeFields(id, fields), "Cuestionario actualizado");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        categoryService.deactivate(id);
        return ApiResponse.ok(null, "Categoria desactivada");
    }
}
