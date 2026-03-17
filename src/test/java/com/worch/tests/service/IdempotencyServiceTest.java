package com.worch.tests.service;

import com.worch.exceptions.IdempotencyFailedException;
import com.worch.exceptions.IdempotencyInProgressException;
import com.worch.model.entity.IdempotencyKey;
import com.worch.model.enums.IdempotencyStatus;
import com.worch.repository.IdempotencyRepository;
import com.worch.service.IdempotencyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Service
@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
public class IdempotencyServiceTest {
    @Mock
    private IdempotencyRepository idempotencyRepository;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private IdempotencyService idempotencyService;

    private final String idempotencyKey = "IdempotencyKey";
    private final String endpoint = "EndPoint";

    @Test
    void checkIdempotencyKey_newKey_createsInProgressAndReturnsNull() {
        setupAuthentication();

        when(idempotencyRepository.findByIdemKey(idempotencyKey)).thenReturn(Optional.empty());

        String result = idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint);

        ArgumentCaptor<IdempotencyKey> captor = ArgumentCaptor.forClass(IdempotencyKey.class);
        verify(idempotencyRepository).save(captor.capture());

        IdempotencyKey savedKey = captor.getValue();

        assertAll(
                () -> assertNull(result),
                () -> assertEquals(idempotencyKey, savedKey.getIdemKey()),
                () -> assertEquals(endpoint, savedKey.getEndpoint()),
                () -> assertEquals(IdempotencyStatus.IN_PROGRESS, savedKey.getResponseStatus()),
                () -> assertNotNull(savedKey.getUserId())
        );
    }

    @Test
    void checkIdempotencyKey_completed_returnsResponseBody() {
        IdempotencyKey key = new IdempotencyKey();
        key.setIdemKey(idempotencyKey);
        key.setEndpoint(endpoint);
        key.setResponseStatus(IdempotencyStatus.COMPLETED);
        key.setResponseBody("Mission Completed");

        when(idempotencyRepository.findByIdemKey(idempotencyKey)).thenReturn(Optional.of(key));

        String result = idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint);

        assertEquals("Mission Completed", result);
    }

    @Test
    void checkIdempotencyKey_failed_throwsException() {
        IdempotencyKey key = new IdempotencyKey();
        key.setIdemKey(idempotencyKey);
        key.setEndpoint(endpoint);
        key.setResponseStatus(IdempotencyStatus.FAILED);

        when(idempotencyRepository.findByIdemKey(idempotencyKey)).thenReturn(Optional.of(key));

        assertThrows(
                IdempotencyFailedException.class,
                () -> idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint)
        );
    }

    @Test
    void checkIdempotencyKey_inProgressThenCompleted_returnsResponseBody() {
        IdempotencyKey inProgressKey = new IdempotencyKey();
        inProgressKey.setIdemKey(idempotencyKey);
        inProgressKey.setEndpoint(endpoint);
        inProgressKey.setResponseStatus(IdempotencyStatus.IN_PROGRESS);
        inProgressKey.setCreatedAt(OffsetDateTime.now());

        IdempotencyKey completedKey = new IdempotencyKey();
        completedKey.setIdemKey(idempotencyKey);
        completedKey.setEndpoint(endpoint);
        completedKey.setResponseStatus(IdempotencyStatus.COMPLETED);
        completedKey.setResponseBody("Mission Completed");
        completedKey.setCreatedAt(OffsetDateTime.now());

        when(idempotencyRepository.findByIdemKey(idempotencyKey))
                .thenReturn(Optional.of(inProgressKey))
                .thenReturn(Optional.of(completedKey));

        String result = idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint);

        assertEquals("Mission Completed", result);
    }

    @Test
    void checkIdempotencyKey_staleInProgress_marksAsFailed() {
        IdempotencyKey staleKey = new IdempotencyKey();
        staleKey.setIdemKey(idempotencyKey);
        staleKey.setEndpoint(endpoint);
        staleKey.setResponseStatus(IdempotencyStatus.IN_PROGRESS);
        staleKey.setCreatedAt(OffsetDateTime.now().minusSeconds(61));

        IdempotencyKey failedKey = new IdempotencyKey();
        failedKey.setIdemKey(idempotencyKey);
        failedKey.setEndpoint(endpoint);
        failedKey.setResponseStatus(IdempotencyStatus.FAILED);
        failedKey.setCreatedAt(OffsetDateTime.now().minusSeconds(61));

        when(idempotencyRepository.findByIdemKey(idempotencyKey))
                .thenReturn(Optional.of(staleKey))
                .thenReturn(Optional.of(failedKey));

        assertThrows(
                IdempotencyFailedException.class,
                () -> idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint)
        );

        verify(idempotencyRepository, atLeastOnce()).save(argThat(saved ->
                saved.getResponseStatus() == IdempotencyStatus.FAILED
        ));
    }

    @Test
    void checkIdempotencyKey_deletedDuringWaiting_throwsEntityNotFound() {
        IdempotencyKey inProgressKey = new IdempotencyKey();
        inProgressKey.setIdemKey(idempotencyKey);
        inProgressKey.setEndpoint(endpoint);
        inProgressKey.setResponseStatus(IdempotencyStatus.IN_PROGRESS);
        inProgressKey.setCreatedAt(OffsetDateTime.now());

        when(idempotencyRepository.findByIdemKey(idempotencyKey))
                .thenReturn(Optional.of(inProgressKey))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint)
        );
    }
    @Test
    void checkIdempotencyKey_alwaysInProgress_throwsInProgressException() {
        IdempotencyKey inProgressKey = new IdempotencyKey();
        inProgressKey.setIdemKey(idempotencyKey);
        inProgressKey.setEndpoint(endpoint);
        inProgressKey.setResponseStatus(IdempotencyStatus.IN_PROGRESS);
        inProgressKey.setCreatedAt(OffsetDateTime.now());

        when(idempotencyRepository.findByIdemKey(idempotencyKey))
                .thenReturn(Optional.of(inProgressKey));

        assertThrows(
                IdempotencyInProgressException.class,
                () -> idempotencyService.checkIdempotencyKey(idempotencyKey, endpoint)
        );

        verify(entityManager, times(10)).clear();
    }


    private void setupAuthentication() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());

        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
