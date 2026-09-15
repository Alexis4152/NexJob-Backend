package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.repository.ServiceOfferingRepository;
import com.nexjob.platform.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;
    private final ServiceOfferingRepository serviceOfferingRepository;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.listActive());
    }

    /** Tipos de servicio especificos ya publicados dentro de una categoria (no hay subcategorias). */
    @GetMapping("/{id}/service-types")
    public ApiResponse<List<String>> serviceTypes(@PathVariable Long id) {
        return ApiResponse.ok(serviceOfferingRepository.findDistinctActiveTitlesByCategory(id));
    }
}
