package org.example.dto;

public record RegisterRequest(String username, String password, String name) {

    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", password=***, name=" + name + "]";
    }
}