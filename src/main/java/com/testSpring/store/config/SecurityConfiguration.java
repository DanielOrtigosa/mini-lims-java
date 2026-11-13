package com.testSpring.store.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.repositories.LimsUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] PROTECTED_PATHS = {
            "/api/**"
    };

    private static final String STATUS_API_PATH = "/api/statuses/**";
    private static final String[] OPERATIONAL_ROLES = {"ADMIN", "TECHNICIAN", "TECNICO"};

    @Bean
    UserDetailsService userDetailsService(LimsUserRepository users) {
        return email -> {
            LimsUser user = users.findLimsUserByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }

            String role = user.getRole() == null ? "USER" : user.getRole().name();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            return User.withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(role)
                    .disabled(!user.isActive())
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret:}") String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException("JWT_SECRET debe configurarse con una clave Base64 de al menos 32 bytes");
        }
        byte[] key = Base64.getDecoder().decode(base64Secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe contener al menos 32 bytes (256 bits)");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        http
                .securityMatcher(PROTECTED_PATHS)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/api/auth/login").permitAll();
                    authorize.requestMatchers("/api/users/**").hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.POST, STATUS_API_PATH).hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.PUT, STATUS_API_PATH).hasRole("ADMIN");
                    authorize.requestMatchers(HttpMethod.DELETE, STATUS_API_PATH).hasRole("ADMIN");
                    authorize.anyRequest().hasAnyRole(OPERATIONAL_ROLES);
                })
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain otherRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
