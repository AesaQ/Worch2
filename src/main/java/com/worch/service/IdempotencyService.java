package com.worch.service;

import com.worch.exceptions.IdempotencyFailedException;
import com.worch.exceptions.IdempotencyInProgressException;
import com.worch.exceptions.IdempotencyInterruptedException;
import com.worch.model.entity.IdempotencyKey;
import com.worch.model.enums.IdempotencyStatus;
import com.worch.repository.IdempotencyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository idempotencyRepository;
    private final EntityManager entityManager;

    @Transactional
    public boolean checkIdempotencyKey(String idemKey, String endpoint) {
        Optional<IdempotencyKey> idempotencyKeyOpt = idempotencyRepository.findByIdemKey(idemKey);

        //ключа не существует, создаём новый
        if (idempotencyKeyOpt.isEmpty()) {
            IdempotencyKey newIdempotencyKey = new IdempotencyKey();
            newIdempotencyKey.setIdemKey(idemKey);

            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = UUID.fromString(jwt.getSubject());
            newIdempotencyKey.setUserId(userId);
            newIdempotencyKey.setResponseStatus(IdempotencyStatus.IN_PROGRESS);
            newIdempotencyKey.setEndpoint(endpoint);
            newIdempotencyKey.setCreatedAt(OffsetDateTime.now());
            idempotencyRepository.save(newIdempotencyKey);
            return true;
        }

        IdempotencyKey idempotencyKey = idempotencyKeyOpt.get();

        //если найденный ключ в процессе
        if (idempotencyKey.getResponseStatus().equals(IdempotencyStatus.IN_PROGRESS)) {

            // если ключ завис более чем на минуту
            if (idempotencyKey.getCreatedAt().isBefore(OffsetDateTime.now().minusSeconds(60))) {
                idempotencyKey.setResponseStatus(IdempotencyStatus.FAILED);
                idempotencyRepository.save(idempotencyKey);
                throw new IdempotencyFailedException("Idempotency failed");
            }

            //встаём в ожидание
            for (int i = 0; i < 10; i++) {
                entityManager.clear();

                IdempotencyKey key = idempotencyRepository.findByIdemKey(idempotencyKey.getIdemKey())
                        .orElseThrow(() ->
                                new EntityNotFoundException("IdempotencyKey был удалён во время ожидания"));

                if (IdempotencyStatus.COMPLETED.equals(key.getResponseStatus())) {
                    return false;
                }

                if (IdempotencyStatus.FAILED.equals(key.getResponseStatus())) {
                    throw new IdempotencyFailedException("Idempotency failed");
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IdempotencyInterruptedException("Waiting for idempotency result was interrupted");
                }
            }

            throw new IdempotencyInProgressException("Idempotency in progress");
        }

        //если запрос в ключе вернул ошибку
        if (idempotencyKey.getResponseStatus().equals(IdempotencyStatus.FAILED)) {
            throw new IdempotencyFailedException("Idempotency failed");
        }

        return false;
    }

    @Transactional
    public void idempotencyKeyComplete(String idemKey) {
        IdempotencyKey key = idempotencyRepository.findByIdemKey(idemKey)
                .orElseThrow(() ->
                        new EntityNotFoundException("IdempotencyKey был удалён во время ожидания"));
        key.setResponseStatus(IdempotencyStatus.COMPLETED);
        idempotencyRepository.save(key);
    }
}
