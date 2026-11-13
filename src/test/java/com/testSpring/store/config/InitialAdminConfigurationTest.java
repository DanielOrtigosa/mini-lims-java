package com.testSpring.store.config;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Role;
import com.testSpring.store.repositories.LimsUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InitialAdminConfigurationTest {

    @Test
    void createsAdminOnlyWhenCredentialsAreConfiguredAndEmailIsNew() throws Exception {
        LimsUserRepository users = mock(LimsUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(users.findLimsUserByEmail("admin@example.com")).thenReturn(null);
        when(encoder.encode("secure-password")).thenReturn("encoded-password");
        CommandLineRunner initializer = new InitialAdminConfiguration().createInitialAdmin(
                users, encoder, "admin@example.com", "secure-password");

        initializer.run();

        var saved = org.mockito.ArgumentCaptor.forClass(LimsUser.class);
        verify(users).save(saved.capture());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
        assertEquals("encoded-password", saved.getValue().getPassword());
        verify(users, never()).save(argThat(user -> "plain-password".equals(user.getPassword())));
    }

    @Test
    void doesNotCreateAdminWhenCredentialsAreMissing() throws Exception {
        LimsUserRepository users = mock(LimsUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        new InitialAdminConfiguration().createInitialAdmin(users, encoder, "", "").run();
        verifyNoInteractions(users, encoder);
    }
}
