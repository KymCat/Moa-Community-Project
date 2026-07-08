package com.example.blogStudy.jwt.redis;

import com.example.blogStudy.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BlacklistTokenService {
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProvider jwtProvider;

    public String createKey(String accessToken) {
        return "Blacklist:" + accessToken;
    }

    // access 토큰 블랙리스트 등록
    public void saveBlackList(String accessToken) {
        stringRedisTemplate.opsForValue().set(
                createKey(accessToken),
                "true",
                jwtProvider.getRemainingTime(accessToken),
                TimeUnit.MILLISECONDS
        );
    }

    // 블랙 리스트 확인
    public boolean isBlackList(String accessToken) {
        return stringRedisTemplate.opsForValue().get(createKey(accessToken)) != null;
    }

}
