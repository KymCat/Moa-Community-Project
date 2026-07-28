package com.example.blogStudy.service;

import com.example.blogStudy.dto.request.PushSubscriptionRequest;
import com.example.blogStudy.entity.PushSubscription;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.repository.PushSubscriptionRepository;
import com.example.blogStudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void subscribe(
            String userId,
            PushSubscriptionRequest request)
    {

        String endpoint = request.getEndpoint();
        validateEndpoint(endpoint);

        String endpointHash = hashEndpoint(endpoint);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        // isPresentOrElse(A,B)
        // A: 값이 있을 때 실행 코드
        // B: 값이 없을 때 실행 코드
        pushSubscriptionRepository.findByEndpointHash(endpointHash)
                .ifPresentOrElse(
                        subscription -> subscription.update(
                                user,
                                request.getKeys().getP256h(),
                                request.getKeys().getAuth()
                        ),
                        () -> pushSubscriptionRepository.save(
                                PushSubscription.create(
                                        user,
                                        endpoint,
                                        endpointHash,
                                        request.getKeys().getP256h(),
                                        request.getKeys().getAuth()
                                )
                        )
                );
    }

    @Transactional
    public void unsubscribe(String userId, String endpoint) {
        validateEndpoint(endpoint);
        String endpointHash = hashEndpoint(endpoint);

        pushSubscriptionRepository
                .deleteByUserIdAndEndpointHash(userId, endpointHash);
    }

    // Endpoint 검증 메서드
    private void validateEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);

            boolean invalidEndpoint =
                    !"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null;

            if (invalidEndpoint) {
                throw new CustomException(ErrorCode.INVALID_REQUEST_VALUE);
            }

        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_VALUE);
        }
    }

    // endpoint sha256 hash
    private String hashEndpoint(String endpoint) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest
                    .digest(endpoint.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }
}
