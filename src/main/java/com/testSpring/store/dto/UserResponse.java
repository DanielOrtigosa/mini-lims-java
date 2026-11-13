package com.testSpring.store.dto;

import com.testSpring.store.entities.LimsUser;

public record UserResponse(int id, String name, String email, String role,
                           String department, String location, boolean active) {
    public static UserResponse from(LimsUser user) {
        return new UserResponse(user.getID(), user.getName(), user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                user.getDepartment(), user.getLocation(), user.isActive());
    }
}
