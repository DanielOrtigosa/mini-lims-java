package com.testSpring.store.config;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Role;
import com.testSpring.store.repositories.LimsUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Configura la creación opcional del administrador inicial. */
@Configuration
public class InitialAdminConfiguration {

    /** Crea la cuenta al arrancar si hay credenciales configuradas. */
    @Bean
    CommandLineRunner createInitialAdmin(
            LimsUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password) {
        return args -> {
            // Evita credenciales vacías o duplicadas.
            if (email.isBlank() || password.isBlank() || users.findLimsUserByEmail(email) != null) return;

            // Prepara la cuenta administradora activa.
            LimsUser admin = new LimsUser();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            // Guarda la contraseña cifrada con BCrypt.
            admin.setPassword(passwordEncoder.encode(password));

            users.save(admin);
        };
    }
}
