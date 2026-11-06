package org.example.service;

import org.example.dto.CreateUserRequest;
import org.example.dto.RegisterRequest;
import org.example.exception.ApiException;
import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class RegistrationService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._-]{3,30}$");
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password1", "12345678", "123456789", "qwertyui",
            "qwerty123", "11111111", "admin123", "letmein1", "welcome1", "iloveyou");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        return create(request.username(), request.password(), request.name(), Role.EMPLOYEE);
    }

    @Transactional
    public User createByAdmin(CreateUserRequest request) {
        Role role = request.role() != null ? request.role() : Role.EMPLOYEE;
        return create(request.username(), request.password(), request.name(), role);
    }

    private User create(String rawUsername, String rawPassword, String name, Role role) {
        if (rawUsername == null || rawPassword == null) {
            throw ApiException.badRequest("Логин и пароль обязательны");
        }
        String username = rawUsername.trim().toLowerCase();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw ApiException.badRequest(
                    "Логин: 3-30 символов, только латинские буквы, цифры, точка, дефис и подчёркивание");
        }
        if (name != null && name.trim().length() > 100) {
            throw ApiException.badRequest("Имя не длиннее 100 символов");
        }
        validatePassword(rawPassword, username);

        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Имя пользователя уже занято");
        }

        User user = new User();
        user.setUsername(username);
        user.setName(name == null || name.isBlank() ? username : name.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));   // только хеш
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void validatePassword(String password, String username) {
        List<String> problems = new ArrayList<>();
        if (password.length() < 8) {
            problems.add("минимум 8 символов");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            problems.add("не длиннее 72 байт");
        }
        if (!password.matches(".*\\p{Ll}.*")) {
            problems.add("хотя бы одна строчная буква");
        }
        if (!password.matches(".*\\p{Lu}.*")) {
            problems.add("хотя бы одна заглавная буква");
        }
        if (!password.matches(".*\\d.*")) {
            problems.add("хотя бы одна цифра");
        }
        if (!password.matches(".*[^\\p{L}\\p{N}\\s].*")) {
            problems.add("хотя бы один спецсимвол");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            problems.add("без пробелов");
        }
        if (password.toLowerCase().contains(username)) {
            problems.add("не должен содержать логин");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            problems.add("слишком распространённый пароль");
        }
        if (!problems.isEmpty()) {
            throw ApiException.badRequest("Ненадёжный пароль: " + String.join("; ", problems));
        }
    }
}