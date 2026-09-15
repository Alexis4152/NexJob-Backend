package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.ForgotPasswordRequest;
import com.nexjob.platform.dto.request.LoginRequest;
import com.nexjob.platform.dto.request.RegisterProviderRequest;
import com.nexjob.platform.dto.request.RegisterRequest;
import com.nexjob.platform.dto.request.ResetPasswordRequest;
import com.nexjob.platform.dto.request.ValidateResetCodeRequest;
import com.nexjob.platform.dto.response.LoginResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.PasswordResetToken;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.Role;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.DuplicateResourceException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.UserMapper;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.repository.PasswordResetTokenRepository;
import com.nexjob.platform.repository.ProviderProfileRepository;
import com.nexjob.platform.repository.RoleRepository;
import com.nexjob.platform.repository.UserRepository;
import com.nexjob.platform.security.JwtTokenProvider;
import com.nexjob.platform.security.RateLimiter;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.AuthService;
import com.nexjob.platform.service.EmailService;
import com.nexjob.platform.service.PostalCodeLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int RESET_CODE_EXPIRATION_MINUTES = 30;
    private static final int RESET_CODE_LENGTH = 6;
    private static final String RESET_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Cuantos codigos puede pedir el mismo correo por hora (independiente de si existe o no,
    // para no filtrar existencia de cuentas por la diferencia de comportamiento).
    private static final int MAX_CODE_REQUESTS_PER_EMAIL = 3;
    private static final Duration CODE_REQUEST_WINDOW = Duration.ofHours(1);
    // Limite mas laxo por IP, para frenar a alguien probando muchos correos distintos.
    private static final int MAX_CODE_REQUESTS_PER_IP = 10;
    // Intentos de validar/canjear un codigo (fuerza bruta) por IP, ventana mas corta.
    private static final int MAX_CODE_ATTEMPTS_PER_IP = 30;
    private static final Duration CODE_ATTEMPT_WINDOW = Duration.ofMinutes(15);
    // Intentos fallidos permitidos sobre un mismo codigo antes de bloquearlo.
    private static final int MAX_ATTEMPTS_PER_CODE = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PostalCodeLookupService postalCodeLookupService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final RateLimiter rateLimiter;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese correo");
        }
        // Registro publico de cliente: el rol SIEMPRE es CLIENT, nunca se acepta del frontend.
        Role clientRole = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() -> new IllegalStateException("Rol CLIENT no configurado"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(clientRole)
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        return LoginResponse.builder().token(token).user(userMapper.toResponse(user)).build();
    }

    @Override
    @Transactional
    public LoginResponse registerProvider(RegisterProviderRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese correo");
        }
        Role providerRole = roleRepository.findByName(RoleName.PROVIDER)
                .orElseThrow(() -> new IllegalStateException("Rol PROVIDER no configurado"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(providerRole)
                .build();
        user = userRepository.save(user);

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
        if (categories.isEmpty()) {
            throw new BusinessException("Selecciona al menos una categoria valida");
        }

        ProviderProfile profile = ProviderProfile.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .bio(request.getBio())
                .yearsExperience(request.getYearsExperience())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .categories(categories)
                .build();
        postalCodeLookupService.lookup(request.getPostalCode()).ifPresent(coords -> {
            profile.setLatitude(coords.latitude());
            profile.setLongitude(coords.longitude());
        });
        providerProfileRepository.save(profile);

        String token = jwtTokenProvider.generateToken(user);
        return LoginResponse.builder().token(token).user(userMapper.toResponse(user)).build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = (User) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(user);
        return LoginResponse.builder().token(token).user(userMapper.toResponse(user)).build();
    }

    @Override
    public User getCurrentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return user;
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String clientIp) {
        String email = request.getEmail().trim().toLowerCase();

        // Los limites se revisan ANTES de buscar el usuario y con el mismo mensaje sin importar
        // el resultado, para no filtrar por temporizacion/respuesta si un correo esta registrado.
        if (!rateLimiter.tryAcquire("forgot-pass:ip:" + clientIp, MAX_CODE_REQUESTS_PER_IP, CODE_REQUEST_WINDOW)) {
            throw new BusinessException("Demasiadas solicitudes. Intenta de nuevo mas tarde");
        }
        long recentRequests = passwordResetTokenRepository
                .countByUser_EmailAndCreatedAtAfter(email, LocalDateTime.now().minus(CODE_REQUEST_WINDOW));
        if (recentRequests >= MAX_CODE_REQUESTS_PER_EMAIL) {
            throw new BusinessException("Demasiadas solicitudes. Intenta de nuevo mas tarde");
        }

        // No se revela si el correo existe o no: siempre se responde igual (evita enumerar
        // cuentas registradas). Si el usuario existe, se genera el codigo y se envia por correo.
        userRepository.findByEmail(email).ifPresent(user -> {
            // Un codigo nuevo invalida cualquier otro que siga pendiente de ese usuario: solo
            // debe existir un codigo vigente a la vez.
            List<PasswordResetToken> pending = passwordResetTokenRepository.findByUser_EmailAndUsedAtIsNull(email);
            pending.forEach(t -> t.setUsedAt(LocalDateTime.now()));
            passwordResetTokenRepository.saveAll(pending);

            String plainCode = generateResetCode();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(hash(plainCode))
                    .expiresAt(LocalDateTime.now().plusMinutes(RESET_CODE_EXPIRATION_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), plainCode);
        });
    }

    @Override
    public void validateResetCode(ValidateResetCodeRequest request, String clientIp) {
        checkAttemptRateLimit(clientIp);
        verifyCode(request.getEmail().trim().toLowerCase(), request.getCode());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request, String clientIp) {
        checkAttemptRateLimit(clientIp);
        PasswordResetToken resetToken = verifyCode(request.getEmail().trim().toLowerCase(), request.getCode());

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    private void checkAttemptRateLimit(String clientIp) {
        if (!rateLimiter.tryAcquire("reset-pass:attempt:ip:" + clientIp, MAX_CODE_ATTEMPTS_PER_IP, CODE_ATTEMPT_WINDOW)) {
            throw new BusinessException("Demasiados intentos. Intenta de nuevo mas tarde");
        }
    }

    /** Busca el (unico) codigo vigente del correo y lo compara por hash; sube el contador de
     * intentos fallidos y bloquea el codigo tras {@link #MAX_ATTEMPTS_PER_CODE} fallos. */
    private PasswordResetToken verifyCode(String email, String code) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser_EmailAndUsedAtIsNull(email).stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow(() -> new BusinessException("El codigo no es valido o ha expirado"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Este codigo ha expirado");
        }
        if (resetToken.getAttempts() >= MAX_ATTEMPTS_PER_CODE) {
            throw new BusinessException("Excediste el numero de intentos. Solicita un codigo nuevo");
        }

        if (!resetToken.getToken().equals(hash(code.trim().toUpperCase()))) {
            resetToken.setAttempts(resetToken.getAttempts() + 1);
            passwordResetTokenRepository.save(resetToken);
            int remaining = MAX_ATTEMPTS_PER_CODE - resetToken.getAttempts();
            throw new BusinessException(remaining > 0
                    ? "El codigo no es valido. Intentos restantes: " + remaining
                    : "Excediste el numero de intentos. Solicita un codigo nuevo");
        }
        return resetToken;
    }

    private String generateResetCode() {
        StringBuilder sb = new StringBuilder(RESET_CODE_LENGTH);
        for (int i = 0; i < RESET_CODE_LENGTH; i++) {
            sb.append(RESET_CODE_ALPHABET.charAt(RANDOM.nextInt(RESET_CODE_ALPHABET.length())));
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
}
