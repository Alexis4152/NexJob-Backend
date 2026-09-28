package com.nexjob.platform.dto.response;

import com.nexjob.platform.enums.IntakeFieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoryIntakeFieldResponse {
    private String id;
    private String label;
    private IntakeFieldType type;
    private List<String> options;
    private String showIfChecked;
    private String showIfFieldId;
    private String showIfValue;
    private String unit;
}
