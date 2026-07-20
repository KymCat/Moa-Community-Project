package com.example.blogStudy.jwt;

import com.example.blogStudy.entity.Role;
import com.example.blogStudy.exception.CustomException;
import com.example.blogStudy.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component  // Bean 등록 : 다른 클래스에서 @RequiredArgsConstructor 주입해서 사용가능
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    private static final String TOKEN_TYPE = "token_type";
    private static final String ROLE_TYPE = "role";

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    // JwtProvider 객체 생성 -> JwtProvider 생성자 -> init() 자동실행 어노테이션
    @PostConstruct
    public void init() {

        // Base64 SecretKey Decode
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);   // HMAC SHA 키 생성(Jwt 서명 키)
    }


    // 사용자 Id를 받아서 Access Token 문자열을 만들어 반환
    public String createAccessToken(String userId, Role role) {
        Date now = new Date();  // 현재 시간
        Date expiration = new Date( // 현재 시간 + Access Token 만료 시간
                now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userId)
                .claim(TOKEN_TYPE, JwtTokenType.ACCESS.name())  // Token type - Access Token
                .claim(ROLE_TYPE, role.name())     // Role type - 역할
                .issuedAt(now)              // iat, 발급일자
                .expiration(expiration)     // exp, 만료일자
                .signWith(key)              // key 를 비밀키로 서명
                .compact();                 // 위 옵션 포함해서 JWT 문자열로 압축
    }


    // 사용자 Id를 받아서 Refresh Token 문자열을 만들어 반환
    public String createRefreshToken(String userId, Role role) {
        Date now = new Date();  // 현재 시간
        Date expiration = new Date( // 현재 시간 + Access Token 만료 시간
                now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userId)            // token 주인 표시
                .claim(TOKEN_TYPE, JwtTokenType.REFRESH.name()) // Token type - Refresh Token
                .claim(ROLE_TYPE, role.name())     // Role type - 역할
                .issuedAt(now)              // iat, 발급일자
                .expiration(expiration)     // exp, 만료일자
                .signWith(key)              // key 를 비밀키로 서명
                .compact();                 // 위 옵션 포함해서 JWT 문자열로 압축
    }

    // token 안에서 payload 반환
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // token 안에서 userId 반환
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    // access token 남은 시간 반환
    public Long getRemainingTime(String accessToken) {
        long remaining = getClaims(accessToken).getExpiration().getTime()
                - System.currentTimeMillis();

        return Math.max(remaining, 0);  // 음수 처리
    }

    // refresh token 만료 시간 반환
    public Long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }

    // AccessToken 검증
    public Claims validateAccessToken(String token) {
        return validateToken(token, JwtTokenType.ACCESS.name());
    }

    // RefreshToken 검증
    public Claims validateRefreshToken(String token) {
        return validateToken(token, JwtTokenType.REFRESH.name());
    }

    // 토큰 검증
    public Claims validateToken(String token, String tokenType) {
        try {
            Claims claims = getClaims(token);
            String expectedTokenType = claims.get(TOKEN_TYPE, String.class);

            if (!tokenType.equals(expectedTokenType)) {
                throw new CustomException(ErrorCode.INVALID_TOKEN_TYPE);
            }

            return claims;

        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);

        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
