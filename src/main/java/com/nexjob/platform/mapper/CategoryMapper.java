package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).icon(c.getIcon())
                .build();
    }
}
