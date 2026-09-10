package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.LoginRequest;
import com.nexjob.platform.dto.request.RegisterProviderRequest;
import com.nexjob.platform.dto.request.RegisterRequest;
import com.nexjob.platform.dto.response.LoginResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.Role;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.RoleName;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.DuplicateResourceException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.UserMapper;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.repository.ProviderProfileRepository;
import com.nexjob.platform.repository.RoleRepository;
import com.nexjob.platform.repository.UserRepository;
import com.nexjob.platform.security.JwtTokenProvider;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

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
                .categories(categories)
                .build();
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
}
