package com.nexjob.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario del sistema (cliente, prestador de servicio o administrador). Implementa
 * {@link UserDetails} directamente para integrarse con Spring Security: el username es el
 * email, y la autoridad otorgada se deriva del {@link Role} asignado. Un usuario con borrado
 * logico ({@code isActive=false}) no puede autenticarse (ver {@link #isEnabled()}).
 */
@Entity
@Table(name = "users")
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class User extends AuditableEntity implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    // Ciudad y codigo postal reutilizados al agendar servicios o pedir cotizaciones, para no
    // pedirselos al cliente cada vez. Edad y foto son puramente informativos, ambos opcionales.
    @Column(length = 100)
    private String city;

    @Column(name = "postal_code", length = 5)
    private String postalCode;

    private Integer age;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ── UserDetails ──────────────────────────────────────────────

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }

    @Override @JsonIgnore public String getPassword()            { return passwordHash; }
    @Override public String getUsername()              { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isEnabled()               { return Boolean.TRUE.equals(getIsActive()); }
}
