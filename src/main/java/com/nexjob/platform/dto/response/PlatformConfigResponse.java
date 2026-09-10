package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PlatformConfigResponse {
    private String platformName;
    private String legalName;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String welcomeMessage;
    private String footerText;
}
