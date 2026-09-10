package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.PlatformConfigRequest;
import com.nexjob.platform.dto.response.PlatformConfigResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PlatformConfigService {
    PlatformConfigResponse get();
    PlatformConfigResponse update(PlatformConfigRequest request);
    PlatformConfigResponse updateLogo(MultipartFile file);
}
