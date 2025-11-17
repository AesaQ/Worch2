package com.worch.service;

import com.worch.exceptions.UserNotAuthenticatedException;
import com.worch.exceptions.UserNotFoundException;
import com.worch.model.entity.User;
import com.worch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("Нет пользователя с таким id: " + id));
    }

    public UUID getCurrentUserId() {
        String currentLogin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if ("anonymousUser".equals(currentLogin)) {
            throw new UserNotAuthenticatedException("User is not authenticated");
        }

        return userRepository.findByLogin(currentLogin)
                .orElseThrow(() -> new UserNotFoundException("Текущий пользователь не найден: " + currentLogin))
                .getId();
    }

    public User getReferenceById(UUID userId) {
        return userRepository.getReferenceById(userId);
    }
}
