package com.testSpring.store.services;

import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.repositories.LimsUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LimsUserServicesTest {
    @Mock LimsUserRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks LimsUserServices service;

    @Test
    void creatingUserStoresEncodedPassword() {
        LimsUser user = new LimsUser();
        user.setPassword("plain-password");
        when(passwordEncoder.encode("plain-password")).thenReturn("bcrypt-hash");

        service.addUser(user);

        assertEquals("bcrypt-hash", user.getPassword());
        verify(repository).save(user);
    }

    @Test
    void missingUserIsRepresentedByEmptyOptional() {
        when(repository.findById(50)).thenReturn(java.util.Optional.empty());
        assertEquals(java.util.Optional.empty(), service.getLimsUser(50));
    }

    @Test
    void updatingUserEncodesNewPasswordAndPreservesExistingPasswordWhenOmitted() {
        LimsUser existing = new LimsUser();
        existing.setPassword("existing-hash");
        when(repository.findById(5)).thenReturn(java.util.Optional.of(existing));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        LimsUser changed = new LimsUser();
        changed.setPassword("new-password");
        service.updateUser(changed, 5);
        assertEquals("new-hash", existing.getPassword());
        existing.setEmail("preserved@example.com");
        existing.setActive(true);

        LimsUser withoutPassword = new LimsUser();
        service.updateUser(withoutPassword, 5);
        assertEquals("new-hash", existing.getPassword());
        assertEquals("preserved@example.com", existing.getEmail());
        assertEquals(true, existing.isActive());
        verify(repository, times(2)).save(any(LimsUser.class));
    }

    @Test
    void loginValidationLooksUpUserByEmailAndHandlesMissingInput() {
        assertEquals(false, service.validateLoginCredentials(null, "pw"));
        assertEquals(false, service.validateLoginCredentials("", "pw"));
        assertEquals(false, service.validateLoginCredentials("missing@example.com", "pw"));
        verify(repository).findLimsUserByEmail("missing@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void addingUserRequiresPassword() {
        LimsUser user = new LimsUser();
        assertThrows(IllegalArgumentException.class, () -> service.addUser(user));
        verify(repository, never()).save(any());
    }

    @Test
    void deletingUserDeactivatesItAndPreservesItsRecordForSampleTraceability() {
        LimsUser technician = new LimsUser();
        technician.setActive(true);
        when(repository.findById(12)).thenReturn(java.util.Optional.of(technician));

        service.deleteUser(12);

        assertEquals(false, technician.isActive());
        verify(repository).save(technician);
        verify(repository, never()).deleteById(any());
    }
}
