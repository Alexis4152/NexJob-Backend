package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.EmailConfigResponse;
import com.nexjob.platform.entity.EmailConfig;
import org.springframework.stereotype.Component;

@Component
public class EmailConfigMapper {
    public EmailConfigResponse toResponse(EmailConfig c) {
        return EmailConfigResponse.builder()
                .enabled(c.getEnabled())
                .smtpHost(c.getSmtpHost())
                .smtpPort(c.getSmtpPort())
                .smtpUsername(c.getSmtpUsername())
                .fromAddress(c.getFromAddress())
                .build();
    }
}
