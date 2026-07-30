package com.example.blogStudy.repository;

import com.example.blogStudy.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    @Query(
            value = """
                    SELECT n
                    FROM Notification n
                    JOIN FETCH n.comment c
                    JOIN FETCH c.user 
                    JOIN FETCH c. post
                    WHERE n.receiver.id = :userId
                    """,
            countQuery = """                   
                        SELECT COUNT(n)
                        FROM Notification n
                        WHERE n.receiver.id = :userId
                        """
    )
    Page<Notification> findAllByReceiverId(
            @Param("userId") String userId,
            Pageable pageable);

    Optional<Notification> findByIdAndReceiverId(
            Long id,
            String userId);

    long countByReceiverIdAndReadAtIsNull(String userId);

    @Query(
            value = """
                    UPDATE Notification n
                    SET n.readAt = :readAt
                    WHERE n.receiver.id = :userId
                    AND n.readAt IS NULL
                    """
    )
    int markAllAsRead(
            @Param("userId") String userId,
            @Param("readAt") Instant readAt
            );

}
