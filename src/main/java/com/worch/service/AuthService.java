package com.worch.service;

import com.worch.config.OAuth2Properties;
import com.worch.exceptions.DatabaseOperationException;
import com.worch.exceptions.InvalidPasswordException;
import com.worch.exceptions.KeycloakOperationException;
import com.worch.exceptions.LoginAlreadyExistsException;
import com.worch.mapper.UserMapper;
import com.worch.model.dto.request.LoginRequest;
import com.worch.model.dto.request.RegisterRequest;
import com.worch.model.dto.response.LoginResponse;
import com.worch.model.entity.User;
import com.worch.repository.UserRepository;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OAuth2Properties oauth2Properties;

    @Transactional
    public void register(@Valid RegisterRequest request) {
        validateLogin(request.login());
        User user = userMapper.toEntity(request);
        user.setCreatedAt(OffsetDateTime.now());

        UUID keycloakUserId;
        try {
            keycloakUserId = keycloakService.createUser(request);
        } catch (KeycloakOperationException e) {
            log.error("Ошибка регистрации пользователя в Keycloak: {}", e.getMessage());
            throw e;
        }

        user.setId(keycloakUserId);

        try {
            userRepository.save(user);
            log.debug("Пользователь успешно зарегистрирован: {}", request.login());
        } catch (Exception e) {
            log.error("Ошибка сохранения пользователя в БД. Откатываем создание пользователя в Keycloak", e);
            keycloakService.rollbackUserCreation(request.login());
            throw new DatabaseOperationException("Ошибка сохранения пользователя", e);
        }
    }

    public LoginResponse login(LoginRequest request) {
        try {
            AccessTokenResponse tokenResponse = KeycloakBuilder.builder()
                    .serverUrl(oauth2Properties.getAuthServerUrl())
                    .realm(oauth2Properties.getRealm())
                    .clientId(oauth2Properties.getClientId())
                    .clientSecret(oauth2Properties.getClientSecret())
                    .username(request.login())
                    .password(request.password())
                    .build()
                    .tokenManager()
                    .getAccessToken();

            return new LoginResponse(
                    tokenResponse.getToken(),
                    tokenResponse.getRefreshToken()
            );
        } catch (Exception e) {
            log.warn("Неуспешная попытка входа для пользователя '{}': {}", request.login(), e.getMessage());
            throw new InvalidPasswordException("Неверные учетные данные");
        }
    }

    private void validateLogin(String login) {
        if (userRepository.existsByLogin(login)) {
            log.warn("Попытка регистрации с уже существующим логином: {}", login);
            throw new LoginAlreadyExistsException("Логин уже занят");
        }
    }
}
