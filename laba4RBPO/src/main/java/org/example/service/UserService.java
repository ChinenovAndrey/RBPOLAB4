package org.example.service;

import org.example.dto.UpdateUserRequest;
import org.example.exception.ApiException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest data, String currentUsername) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        boolean self = user.getUsername().equalsIgnoreCase(currentUsername);
        boolean roleChange = data.role() != null && data.role() != user.getRole();
        if (self && (roleChange || Boolean.FALSE.equals(data.enabled()))) {
            throw ApiException.conflict("Нельзя блокировать или менять роль собственной учётной записи");
        }
        if (data.name() != null) {
            if (data.name().isBlank() || data.name().trim().length() > 100) {
                throw ApiException.badRequest("Имя не должно быть пустым или длиннее 100 символов");
            }
            user.setName(data.name().trim());
        }
        if (data.role() != null) {
            user.setRole(data.role());
        }
        if (data.enabled() != null) {
            user.setEnabled(data.enabled());
        }
        return user;
    }

    public boolean deleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw ApiException.conflict("Нельзя удалить собственную учётную запись");
        }
        userRepository.deleteById(id);
        return true;
    }
}