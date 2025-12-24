package com.victusstore.repository;

import com.victusstore.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
    
    Optional<IdempotencyKey> findByKey(String key);
    
    Optional<IdempotencyKey> findByKeyAndUserEmail(String key, String userEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IdempotencyKey> findByKeyAndUserEmailForUpdate(String key, String userEmail);
    
    @Modifying
    @Query("DELETE FROM IdempotencyKey i WHERE i.expiresAt < :now")
    int deleteExpiredKeys(@Param("now") LocalDateTime now);
}

