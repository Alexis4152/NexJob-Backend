package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.CategoryRequest;
import com.nexjob.platform.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> listActive();
    Page<CategoryResponse> adminList(String q, Pageable pageable);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void deactivate(Long id);
}
