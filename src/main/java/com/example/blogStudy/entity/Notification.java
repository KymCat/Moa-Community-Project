package com.example.blogStudy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_notification_comment_id",    // 댓글 하나당 알림 한개
                        columnNames = "comment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_notification_receiver_created", // 알림순 조회 대비
                        columnList = "receiver_id, created_at"
                ),
                @Index(
                        name = "idx_notification_receiver_read", // 읽지 않은 알림개수 조회 대비
                        columnList = "receiver_id, read_at"
                )
        }

)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "read_at")
    private Instant readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    public static Notification create(User user, Comment comment) {
        Notification nf = new Notification();
        nf.user = user;
        nf.comment = comment;

        return nf;
    }

    public boolean isRead() {
        return readAt != null;
    }

    // 읽음 표시
    public void markAsRead() {
        if (readAt == null)
            readAt = Instant.now();
    }

}
