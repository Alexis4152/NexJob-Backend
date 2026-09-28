package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.CategoryIntakeFieldResponse;
import com.nexjob.platform.dto.response.CategoryResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.CategoryIntakeField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).icon(c.getIcon())
                .intakeFields(toIntakeFieldResponses(c.getIntakeFields()))
                .build();
    }

    public List<CategoryIntakeFieldResponse> toIntakeFieldResponses(List<CategoryIntakeField> fields) {
        if (fields == null) {
            return List.of();
        }
        return fields.stream()
                .map(f -> CategoryIntakeFieldResponse.builder()
                        .id(f.getId()).label(f.getLabel()).type(f.getType()).options(f.getOptions())
                        .showIfChecked(f.getShowIfChecked())
                        .showIfFieldId(f.getShowIfFieldId())
                        .showIfValue(f.getShowIfValue())
                        .unit(f.getUnit())
                        .build())
                .toList();
    }
}
