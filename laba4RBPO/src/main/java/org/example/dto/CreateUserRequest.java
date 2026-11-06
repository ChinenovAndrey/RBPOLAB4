package org.example.dto;

import org.example.model.Role;

public record CreateUserRequest(String username, String password, String name, Role role) {

    @Override
    public String toString() {
        return "CreateUserRequest[username=" + username + ", password=***, name=" + name + ", role=" + role + "]";
    }
}