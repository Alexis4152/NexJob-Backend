package com.nexjob.platform.config;

import com.nexjob.platform.security.CustomUserDetailsService;
import com.nexjob.platform.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * API stateless (JWT), sin CSRF, con endpoints publicos de catalogo/auth/soporte y el resto
 * de la API protegida por rol. {@code /api/admin/**} exige ROLE_ADMIN, {@code /api/provider/**}
 * exige ROLE_PROVIDER; el resto de rutas autenticadas solo exigen un usuario valido (la
 * autorizacion fina por propietario del recurso, ej. "solo mis contrataciones", se valida en
 * el service).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Excepcion dentro de /api/auth/** (que es publico): renovar el token
                        // exige que el token actual siga siendo valido, si no de que serviria.
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        // Registro de visitas anonimas (sin login): ver PublicAnalyticsController.
                        .requestMatchers(HttpMethod.POST, "/api/public/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/support/tickets").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/provider/**").hasRole("PROVIDER")
                        .anyRequest().authenticated()
                )
                // Sin esto, Spring Security responde 403 tanto para "no autenticado" (token
                // ausente/invalido/vencido) como para "autenticado pero sin el rol necesario",
                // y el frontend solo puede distinguir "sesion expirada" a partir de un 401 (ver
                // interceptor de axios). Spring tambien enruta un AccessDeniedException al
                // AuthenticationEntryPoint (no al AccessDeniedHandler) cuando considera la
                // solicitud "anonima" segun su propia logica interna, y ademas limpia el
                // SecurityContext justo antes de invocar el entry point -- por eso no se puede
                // usar SecurityContextHolder aqui para distinguir los casos; se usa en su lugar
                // el atributo que JwtAuthFilter deja en el request si el JWT si autentico.
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    boolean hasRealSession = Boolean.TRUE.equals(request.getAttribute(JwtAuthFilter.AUTHENTICATED_ATTRIBUTE));
                    response.setStatus(hasRealSession ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    String message = hasRealSession
                            ? "No tienes permiso para realizar esta accion"
                            : "No has iniciado sesion o tu sesion expiro";
                    response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
                }))
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
