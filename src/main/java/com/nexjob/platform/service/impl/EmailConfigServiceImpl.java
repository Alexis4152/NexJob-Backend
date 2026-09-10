package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.EmailConfigRequest;
import com.nexjob.platform.dto.response.EmailConfigResponse;
import com.nexjob.platform.entity.EmailConfig;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.EmailConfigMapper;
import com.nexjob.platform.repository.EmailConfigRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.EmailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;
    private final EmailConfigMapper mapper;

    @Override
    public EmailConfigResponse get() {
        return mapper.toResponse(singleton());
    }

    @Override
    @Transactional
    public EmailConfigResponse update(EmailConfigRequest request) {
        EmailConfig config = singleton();
        config.setEnabled(request.getEnabled());
        config.setSmtpHost(request.getSmtpHost());
        config.setSmtpPort(request.getSmtpPort());
        config.setSmtpUsername(request.getSmtpUsername());
        // Solo se sobreescribe la contrasena si se envio una nueva, para no borrarla cada vez
        // que el admin guarda el formulario sin querer cambiarla (el frontend nunca la muestra).
        if (StringUtils.hasText(request.getSmtpPassword())) {
            config.setSmtpPassword(request.getSmtpPassword());
        }
        config.setFromAddress(request.getFromAddress());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return mapper.toResponse(emailConfigRepository.save(config));
    }

    private EmailConfig singleton() {
        return emailConfigRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La configuracion de correo no esta inicializada"));
    }
}
