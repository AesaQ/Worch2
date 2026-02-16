package com.worch.exceptions;

import com.worch.model.dto.response.ErrorResponse;
import com.worch.model.dto.response.ValidationErrorResponse;
import com.worch.util.EnumUtil;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "Произошла внутренняя ошибка";
    private static final String SESSION_ERROR_DEFAULT = "Ошибка сессии";
    private static final String ACCESS_DENIED_MESSAGE = "Доступ запрещён";

    @ExceptionHandler(LoginAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLoginAlreadyExists(LoginAlreadyExistsException ex) {
        log.warn("Login conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ChoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChoiceNotFound(ChoiceNotFoundException ex) {
        log.warn("Choice not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ChoiceOptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChoiceOptionNotFound(ChoiceOptionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ChoiceOptionMismatchException.class)
    public ResponseEntity<ErrorResponse> handleChoiceOptionMismatch(ChoiceOptionMismatchException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        if (ex.getCause() != null && ex.getCause()
                .getCause() instanceof InvalidEnumValueException iex) {
            Map<String, List<String>> errors = Map.of(
                    "enum", List.of(
                            "Недопустимое значение: %s. Поддерживаемые значения: %s".formatted(
                                    iex.getInvalidValue(),
                                    String.join(", ", EnumUtil.getEnumValues(iex.getEnumClass()))
                            )
                    )
            );
            return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
        }

        Map<String, List<String>> genericError = Map.of(
                "request", List.of("Неверный формат запроса")
        );
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(genericError));
    }

    @ExceptionHandler(UserSessionException.class)
    public ResponseEntity<ErrorResponse> handleUserSessionException(UserSessionException ex) {
        log.warn("Ошибка сессии: {}", ex.getMessage());
        String userMessage = extractUserFriendlyMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(userMessage));
    }

    @ExceptionHandler(KeycloakOperationException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakOperation(KeycloakOperationException ex) {
        log.error("Keycloak operation failed: {}", ex.getMessage());
        String userMessage = resolveKeycloakUserMessage(ex);
        HttpStatus status = determineKeycloakHttpStatus(ex.getStatusCode());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(userMessage));
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseOperation(DatabaseOperationException ex) {
        log.error("Database operation failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ошибка сохранения данных. Попробуйте позже"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ACCESS_DENIED_MESSAGE));
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthenticated(UserNotAuthenticatedException ex) {
        log.warn("Unauthenticated user attempt: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Требуется аутентификация"));
    }

    @ExceptionHandler(DuplicateVoteException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateVote(DuplicateVoteException ex) {
        log.warn("Duplicate vote attempt: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Вы уже проголосовали за этот выбор"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(INTERNAL_ERROR_MESSAGE));
    }


    // ==== Вспомогательные методы ====

    private String extractUserFriendlyMessage(String technicalMessage) {
        if (technicalMessage == null || !containsTechnicalDetails(technicalMessage)) {
            return technicalMessage != null ? technicalMessage : SESSION_ERROR_DEFAULT;
        }

        return switch (getErrorType(technicalMessage)) {
            case INVALID_TOKEN -> "Сессия завершена или токен недействителен";
            case BAD_REQUEST -> "Некорректные данные для завершения сессии";
            case UNAUTHORIZED -> "Недостаточно прав для завершения сессии";
            case SERVER_ERROR -> "Временная ошибка сервера. Попробуйте позже";
            default -> "Не удалось завершить сессию";
        };
    }

    private String resolveKeycloakUserMessage(KeycloakOperationException ex) {
        return switch (ex.getStatusCode()) {
            case 409 -> "Пользователь с такими данными уже существует";
            case 400 -> "Некорректные данные пользователя";
            case 401, 403 -> "Ошибка авторизации в системе";
            default -> ex.getStatusCode() >= 500 ?
                    "Временная ошибка сервера аутентификации" :
                    "Ошибка при работе с системой аутентификации";
        };
    }

    private HttpStatus determineKeycloakHttpStatus(int statusCode) {
        return switch (statusCode) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 409 -> HttpStatus.CONFLICT;
            default -> statusCode >= 500 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        };
    }

    private boolean containsTechnicalDetails(String message) {
        return message.matches(
                ".*(Bad Request|invalid_grant|HTTP|RestClientException|Keycloak API|SQLException).*");
    }

    private ErrorType getErrorType(String message) {
        if (message.contains("invalid_grant") || message.contains("Invalid refresh token")) {
            return ErrorType.INVALID_TOKEN;
        }
        if (message.contains("400 Bad Request")) {
            return ErrorType.BAD_REQUEST;
        }
        if (message.contains("401") || message.contains("403")) {
            return ErrorType.UNAUTHORIZED;
        }
        if (message.contains("500")) {
            return ErrorType.SERVER_ERROR;
        }
        return ErrorType.UNKNOWN;
    }

    private enum ErrorType {
        INVALID_TOKEN, BAD_REQUEST, UNAUTHORIZED, SERVER_ERROR, UNKNOWN
    }
}
