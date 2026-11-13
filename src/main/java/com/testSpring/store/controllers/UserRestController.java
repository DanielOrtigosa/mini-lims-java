package com.testSpring.store.controllers;

import com.testSpring.store.dto.ActivationRequest;
import com.testSpring.store.dto.UserRequest;
import com.testSpring.store.dto.UserResponse;
import com.testSpring.store.dto.UserUpdateRequest;
import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Role;
import com.testSpring.store.services.LimsUserServices;
import com.testSpring.store.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final LimsUserServices users;

    public UserRestController(LimsUserServices users) { this.users = users; }

    @GetMapping
    public org.springframework.data.domain.Page<UserResponse> list(Pageable pageable) {
        return users.getAllLimsUsers(pageable).map(UserResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        LimsUser user = new LimsUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(normalizeRole(request.role()));
        user.setDepartment(request.department());
        user.setLocation(request.location());
        user.setPassword(request.password());
        users.addUser(user);
        return UserResponse.from(user);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable int id, @Valid @RequestBody UserUpdateRequest request) {
        LimsUser changes = new LimsUser();
        changes.setName(request.name());
        changes.setRole(request.role() == null ? null : normalizeRole(request.role()));
        changes.setDepartment(request.department());
        changes.setLocation(request.location());
        changes.setPassword(request.password());
        users.updateUser(changes, id);
        return users.getLimsUser(id).map(UserResponse::from).orElseThrow();
    }

    @PatchMapping("/{id}/activation")
    public UserResponse setActivation(@PathVariable int id, @Valid @RequestBody ActivationRequest request) {
        if (request.active()) users.activateUser(id);
        else users.deactivateUser(id);
        return users.getLimsUser(id).map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + id));
    }

    private Role normalizeRole(String role) {
        try {
            return Role.from(role);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El rol debe ser ADMIN o TECHNICIAN", ex);
        }
    }
}
