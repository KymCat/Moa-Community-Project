package com.example.blogStudy.auth;

import com.example.blogStudy.dto.request.LoginRequest;
import com.example.blogStudy.entity.Role;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import com.example.blogStudy.jwt.JwtProperties;
import com.example.blogStudy.jwt.JwtProvider;
import com.example.blogStudy.jwt.JwtTokenResult;
import com.example.blogStudy.jwt.redis.BlacklistTokenService;
import com.example.blogStudy.repository.UserRepository;
import com.example.blogStudy.jwt.redis.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistTokenService blacklistTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private static final String ROLE_TYPE = "role";

    // 로그인
    @Transactional
    public JwtTokenResult login(LoginRequest dto) {
        // 1. ID 확인
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 확인
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new CustomException(ErrorCode.INVALID_PASSWORD);

        // 3. 토큰 생성 (Jwt Access, Refresh)
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getRole());

        // 4. Refresh 토큰 Redis 에 저장
        refreshTokenService.save(
                user.getId(),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        return new JwtTokenResult(
                accessToken,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration());
    }


    // 로그아웃
    @Transactional
    public void logout(String userId, String accessToken, String refreshToken) {
        Claims refreshClaims = jwtProvider.validateRefreshToken(refreshToken);
        String refreshUserId = refreshClaims.getSubject();

        // 현재 로그인한 id 와 각 토큰에서 추출한 id 비교
        if (!userId.equals(refreshUserId)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_OWNER);
        }

        // RefreshToken 유효성 검사
        if (Boolean.FALSE.equals(refreshTokenService.isValid(userId, refreshToken))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        refreshTokenService.delete(userId);
        blacklistTokenService.saveBlackList(accessToken);
    }


    // refresh token 재발행
    @Transactional
    public JwtTokenResult reissue(String token) {

        // 1. refresh token 검증
        Claims claims = jwtProvider.validateRefreshToken(token);

        // 2. dto 데이터에서 user id 추출 후, DB 조회 및 user role 추출
        String userId = claims.getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String role = claims.get(ROLE_TYPE, String.class);
        Role userRole = Role.valueOf(role);

        // 3. redis refresh token 과 비교
        if(Boolean.FALSE.equals(refreshTokenService.isValid(user.getId(), token)))
            throw new CustomException(ErrorCode.INVALID_TOKEN);

        // 4. access, refresh 재발행
        String accessToken = jwtProvider.createAccessToken(user.getId(), userRole);
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), userRole);

        // 5. redis 에 저장
        refreshTokenService.save(
                userId,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        return new JwtTokenResult(
                accessToken,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration());
    }

}
