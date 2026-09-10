package com.nexjob.platform.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BookingEvidenceRequest {
    private MultipartFile file;
    private String description;
}
