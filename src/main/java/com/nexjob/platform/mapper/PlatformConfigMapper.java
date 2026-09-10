package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.PlatformConfigResponse;
import com.nexjob.platform.entity.PlatformConfig;
import org.springframework.stereotype.Component;

@Component
public class PlatformConfigMapper {
    public PlatformConfigResponse toResponse(PlatformConfig c) {
        return PlatformConfigResponse.builder()
                .platformName(c.getPlatformName())
                .legalName(c.getLegalName())
                .contactEmail(c.getContactEmail())
                .contactPhone(c.getContactPhone())
                .logoUrl(c.getLogoUrl())
                .primaryColor(c.getPrimaryColor())
                .secondaryColor(c.getSecondaryColor())
                .welcomeMessage(c.getWelcomeMessage())
                .footerText(c.getFooterText())
                .build();
    }
}
