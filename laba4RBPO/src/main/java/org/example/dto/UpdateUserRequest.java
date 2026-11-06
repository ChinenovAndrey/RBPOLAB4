package org.example.dto;

import org.example.model.Role;

public record UpdateUserRequest(String name, Role role, Boolean enabled) {
}