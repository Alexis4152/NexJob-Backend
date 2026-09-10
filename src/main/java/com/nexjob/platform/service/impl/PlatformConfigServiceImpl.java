package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.PlatformConfigRequest;
import com.nexjob.platform.dto.response.PlatformConfigResponse;
import com.nexjob.platform.entity.PlatformConfig;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.PlatformConfigMapper;
import com.nexjob.platform.repository.PlatformConfigRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.FileStorageService;
import com.nexjob.platform.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PlatformConfigServiceImpl implements PlatformConfigService {

    private final PlatformConfigRepository platformConfigRepository;
    private final FileStorageService fileStorageService;
    private final PlatformConfigMapper mapper;

    @Override
    public PlatformConfigResponse get() {
        return mapper.toResponse(singleton());
    }

    @Override
    @Transactional
    public PlatformConfigResponse update(PlatformConfigRequest request) {
        PlatformConfig config = singleton();
        config.setPlatformName(request.getPlatformName());
        config.setLegalName(request.getLegalName());
        config.setContactEmail(request.getContactEmail());
        config.setContactPhone(request.getContactPhone());
        config.setPrimaryColor(request.getPrimaryColor());
        config.setSecondaryColor(request.getSecondaryColor());
        config.setWelcomeMessage(request.getWelcomeMessage());
        config.setFooterText(request.getFooterText());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return mapper.toResponse(platformConfigRepository.save(config));
    }

    @Override
    @Transactional
    public PlatformConfigResponse updateLogo(MultipartFile file) {
        PlatformConfig config = singleton();
        config.setLogoUrl(fileStorageService.store(file, "platform"));
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return mapper.toResponse(platformConfigRepository.save(config));
    }

    private PlatformConfig singleton() {
        return platformConfigRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La configuracion de la plataforma no esta inicializada"));
    }
}
