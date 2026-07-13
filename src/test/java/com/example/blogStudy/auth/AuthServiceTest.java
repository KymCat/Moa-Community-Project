package com.example.blogStudy.auth;

import com.example.blogStudy.dto.request.LoginRequest;
import com.example.blogStudy.entity.User;
import com.example.blogStudy.jwt.JwtProperties;
import com.example.blogStudy.jwt.JwtProvider;
import com.example.blogStudy.jwt.JwtTokenResult;
import com.example.blogStudy.jwt.redis.BlacklistTokenService;
import com.example.blogStudy.jwt.redis.RefreshTokenService;
import com.example.blogStudy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private JwtProvider jwtProvider;
    @Mock private JwtProperties jwtProperties;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private BlacklistTokenService blacklistTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;

    @InjectMocks    // @Mock 객체들을 AuthService 생성자에 주입
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        String id = "user1234";
        String password = "user1234@";
        String encodedPassword = passwordEncoder.encode(password);
        String name = "닉네임";

        LoginRequest loginRequest = new LoginRequest(id, password);
        User user = User.create(id, encodedPassword, name);

        // Optional.of() : null 이 아닌 값을 매핑
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(id)).willReturn("accessToken");
        given(jwtProvider.createRefreshToken(id)).willReturn("refreshToken");
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(600000L);

        // when
        JwtTokenResult result = authService.login(loginRequest);

        // then
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals(600000L, result.getRefreshTokenExpiration());

        // refreshTokenService.save() 호출 검증
        then(refreshTokenService).should()
                .save(id, "refreshToken", 600000L);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout() {
        // given
        String userId = "user1234";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(jwtProvider.getUserId(refreshToken)).willReturn(userId);

        // when
        authService.logout(accessToken,refreshToken);

        // then
        then(refreshTokenService).should().delete(userId);
        then(blacklistTokenService).should().saveBlackList(accessToken);
    }


    @Test
    @DisplayName("재발행 성공")
    void reissue() {
        // given
        String userId = "user1234";
        String nickname = "nickname";
        String refreshToken = "refreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        long refreshTokenExpiration = 600000L;

        given(jwtProvider.getUserId(refreshToken)).willReturn(userId);
        given(jwtProvider.getNickname(refreshToken)).willReturn(nickname);
        given(refreshTokenService.isValid(userId, refreshToken)).willReturn(true);
        given(jwtProvider.createAccessToken(userId)).willReturn(newAccessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(newRefreshToken);
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(refreshTokenExpiration);

        // when
        JwtTokenResult result = authService.reissue(refreshToken);

        // then
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
        assertEquals(600000L, result.getRefreshTokenExpiration());

        then(refreshTokenService).should()
                .save(userId, newRefreshToken, refreshTokenExpiration);
    }
}