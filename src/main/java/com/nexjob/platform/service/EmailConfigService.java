package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.EmailConfigRequest;
import com.nexjob.platform.dto.response.EmailConfigResponse;

public interface EmailConfigService {
    EmailConfigResponse get();
    EmailConfigResponse update(EmailConfigRequest request);
}
