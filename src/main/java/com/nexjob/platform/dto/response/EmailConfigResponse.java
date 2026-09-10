package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailConfigResponse {
    private Boolean enabled;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String fromAddress;
}
