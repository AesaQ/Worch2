package com.worch.service;

import com.worch.config.OAuth2Properties;
import com.worch.exceptions.KeycloakOperationException;
import com.worch.model.dto.request.RegisterRequest;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.default-role:user}")
    private String defaultRole;

    private final Keycloak keycloakAdmin;
    private final OAuth2Properties oauth2Properties;


    public UUID createUser(RegisterRequest request) {
        UserRepresentation userRep = buildUserRepresentation(request);
        String userId = createUserInKeycloak(userRep);
        assignDefaultRole(userId);
        log.debug("Пользователь '{}' создан в Keycloak с ролью '{}'", request.login(), defaultRole);
        return UUID.fromString(userId);
    }

    public void rollbackUserCreation(String login) {
        try {
            List<UserRepresentation> users = getRealmResource().users().search(login);
            if (!users.isEmpty()) {
                getRealmResource().users().delete(users.get(0).getId());
                log.debug("Откат: пользователь '{}' удалён из Keycloak", login);
            }
        } catch (Exception e) {
            log.error("Не удалось откатить создание пользователя {}: {}", login, e.getMessage());
            throw new KeycloakOperationException("Не удалось откатить создание пользователя", 500);
        }
    }

    private UserRepresentation buildUserRepresentation(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.login());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(createPasswordCredential(request.password())));
        return user;
    }

    private CredentialRepresentation createPasswordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    private String createUserInKeycloak(UserRepresentation user) {
        try (Response response = getRealmResource().users().create(user)) {
            validateResponse(response);
            return extractUserId(response);
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя в Keycloak: {}", e.getMessage());
            throw new KeycloakOperationException("Ошибка при создании пользователя в Keycloak", 500);
        }
    }

    private void assignDefaultRole(String userId) {
        try {
            RoleRepresentation role = getRoleRepresentation();
            getRealmResource().users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
            log.debug("Роль '{}' успешно назначена пользователю с userId: {}", defaultRole, userId);
        } catch (Exception e) {
            log.error("Ошибка назначения роли '{}' пользователю {}: {}", defaultRole, userId, e.getMessage());
            throw new KeycloakOperationException("Ошибка назначения роли пользователю: " + e.getMessage(), 500);
        }
    }

    private RoleRepresentation getRoleRepresentation() {
        RoleRepresentation role = getRealmResource().roles().get(defaultRole).toRepresentation();
        if (role == null) {
            throw new KeycloakOperationException("Роль не найдена: " + defaultRole, 404);
        }
        return role;
    }

    private void validateResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            String error = response.readEntity(String.class);
            log.error("Ошибка Keycloak API ({}): {}", response.getStatus(), error);
            throw new KeycloakOperationException("Ошибка Keycloak API: " + error, response.getStatus());
        }
        log.debug("Ответ Keycloak: пользователь создан успешно (HTTP {})", response.getStatus());
    }

    private String extractUserId(Response response) {
        String location = response.getHeaderString("Location");
        if (location == null || !location.contains("/users/")) {
            throw new KeycloakOperationException("Некорректный ответ Keycloak, не удалось получить userId", 500);
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private RealmResource getRealmResource() {
        return keycloakAdmin.realm(oauth2Properties.getRealm());
    }
}
