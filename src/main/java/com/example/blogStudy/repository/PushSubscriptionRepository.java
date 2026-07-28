package com.example.blogStudy.repository;

import com.example.blogStudy.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository
        extends JpaRepository<PushSubscription, Long> {

    // 동일한 브라우저 구독이 이미 존재하는지 확인
    Optional<PushSubscription> findByEndpointHash(
            String endpointHash
    );

    // 해당 사용자의 모든 기기로 Push 전송
    List<PushSubscription> findAllByUserId(
            String userId
    );

    // 로그아웃한 현재 사용자의 특정 브라우저 구독만 삭제
    long deleteByUserIdAndEndpointHash(
            String userId,
            String endpointHash
    );
}
