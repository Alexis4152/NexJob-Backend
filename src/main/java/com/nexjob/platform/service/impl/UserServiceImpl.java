package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.UserUpdateRequest;
import com.nexjob.platform.dto.response.UserResponse;
import com.nexjob.platform.entity.EmailVerificationCode;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.UserMapper;
import com.nexjob.platform.repository.EmailVerificationCodeRepository;
import com.nexjob.platform.repository.UserRepository;
import com.nexjob.platform.security.RateLimiter;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.EmailService;
import com.nexjob.platform.service.FileStorageService;
import com.nexjob.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int VERIFY_CODE_EXPIRATION_MINUTES = 10;
    private static final int VERIFY_CODE_LENGTH = 6;
    private static final String VERIFY_CODE_ALPHABET = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS_PER_CODE = 5;
    // Enfriamiento entre reenvios (evita spam de correos con solo darle varias veces a "reenviar").
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    // Tope mas amplio por hora, independiente del enfriamiento.
    private static final int MAX_CODE_REQUESTS_PER_HOUR = 5;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final RateLimiter rateLimiter;

    @Override
    @Transactional
    public UserResponse updateMyProfile(UserUpdateRequest request) {
        User user = currentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCity(request.getCity());
        user.setPostalCode(request.getPostalCode());
        user.setAge(request.getAge());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateMyPhoto(MultipartFile file) {
        User user = currentUser();
        user.setProfileImageUrl(fileStorageService.store(file, "users"));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> adminList(RoleName role, String q, Pageable pageable) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findByRole_Name(role, pageable)
                : userRepository.findByRole_NameAndEmailContainingIgnoreCase(role, q.trim(), pageable);
        return page.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse adminSetActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        user.setIsActive(active);
        user.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void sendEmailVerificationCode() {
        User user = currentUser();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Tu correo ya esta verificado");
        }
        if (!rateLimiter.tryAcquire("email-verify:cooldown:user:" + user.getId(), 1, RESEND_COOLDOWN)) {
            throw new BusinessException("Espera unos segundos antes de volver a pedir un codigo");
        }
        long recentRequests = emailVerificationCodeRepository
                .countByUser_IdAndCreatedAtAfter(user.getId(), LocalDateTime.now().minusHours(1));
        if (recentRequests >= MAX_CODE_REQUESTS_PER_HOUR) {
            throw new BusinessException("Demasiadas solicitudes. Intenta de nuevo mas tarde");
        }

        // Un codigo nuevo invalida cualquier otro que siga pendiente: solo debe existir uno vigente.
        List<EmailVerificationCode> pending = emailVerificationCodeRepository.findByUser_IdAndUsedAtIsNull(user.getId());
        pending.forEach(c -> c.setUsedAt(LocalDateTime.now()));
        emailVerificationCodeRepository.saveAll(pending);

        String plainCode = generateVerificationCode();
        EmailVerificationCode code = EmailVerificationCode.builder()
                .user(user)
                .code(hash(plainCode))
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFY_CODE_EXPIRATION_MINUTES))
                .build();
        emailVerificationCodeRepository.save(code);
        emailService.sendEmailVerificationCode(user.getEmail(), plainCode);
    }

    @Override
    @Transactional
    public UserResponse verifyEmailCode(String code) {
        User user = currentUser();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Tu correo ya esta verificado");
        }

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findByUser_IdAndUsedAtIsNull(user.getId()).stream()
                .max(Comparator.comparing(EmailVerificationCode::getCreatedAt))
                .orElseThrow(() -> new BusinessException("Solicita un codigo antes de verificar"));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Este codigo ha expirado, solicita uno nuevo");
        }
        if (verificationCode.getAttempts() >= MAX_ATTEMPTS_PER_CODE) {
            throw new BusinessException("Excediste el numero de intentos. Solicita un codigo nuevo");
        }
        if (!verificationCode.getCode().equals(hash(code.trim()))) {
            verificationCode.setAttempts(verificationCode.getAttempts() + 1);
            emailVerificationCodeRepository.save(verificationCode);
            int remaining = MAX_ATTEMPTS_PER_CODE - verificationCode.getAttempts();
            throw new BusinessException(remaining > 0
                    ? "El codigo no es valido. Intentos restantes: " + remaining
                    : "Excediste el numero de intentos. Solicita un codigo nuevo");
        }

        verificationCode.setUsedAt(LocalDateTime.now());
        emailVerificationCodeRepository.save(verificationCode);
        user.setEmailVerified(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    private String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(VERIFY_CODE_LENGTH);
        for (int i = 0; i < VERIFY_CODE_LENGTH; i++) {
            sb.append(VERIFY_CODE_ALPHABET.charAt(RANDOM.nextInt(VERIFY_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return user;
    }
}
