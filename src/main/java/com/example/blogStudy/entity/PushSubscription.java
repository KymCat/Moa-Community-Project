package com.example.blogStudy.entity;

import com.example.blogStudy.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pushSubscription",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_push_subscription_endpoint_hash",    // 구독 중복 방지, endpoint 조회
                        columnNames = "endpoint_hash"
                )
        },
        indexes = {
                @Index(
                        name = "idx_push_subscription_user_id", // 사용자의 구독 전체 조회 대비
                        columnList = "user_id"
                )
        }
)
public class PushSubscription  extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "endpoint_hash", nullable = false, length = 64)
    private String endpointHash;

    @Column(nullable = false, length = 128)
    private String p256dh;

    @Column(nullable = false, length = 64)
    private String auth;

    // Fk
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static PushSubscription create(
            User user,
            String endpoint,
            String endpointHash,
            String p256dh,
            String auth
    ) {
       PushSubscription ps = new PushSubscription();
       ps.user = user;
       ps.endpoint = endpoint;
       ps.endpointHash = endpointHash;
       ps.p256dh = p256dh;
       ps.auth = auth;

        return ps;
    }

    public void update(User user, String p256dh, String auth) {
        this.user = user;
        this.p256dh = p256dh;
        this.auth = auth;
    }
}
