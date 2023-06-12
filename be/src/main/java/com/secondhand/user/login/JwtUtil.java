package com.secondhand.user.login;

import com.secondhand.user.entity.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String secret; // 시크릿 키를 설정

    public String createToken(User loggedInUser) {
        log.info(secret);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("login_member")
                .claim("userProfile", loggedInUser)
                .setExpiration(new Date((new Date()).getTime() + 3600000)) // 토큰의 만료일을 설정 : 현재 1시간
                .signWith(SignatureAlgorithm.HS256, secret) // HS256 알고리즘과 시크릿 키를 사용하여 서명
                .compact(); // 토큰을 생성하세요.
    }

    public boolean validateTokenIsExpired(String token) {

        try {
            // 토큰 검증
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);

            // 토큰이 만료되었는지 확인
            if (claimsJws.getBody().getExpiration().before(new Date())) {
                return false; // 토큰이 만료되었습니다.
            }

            return true; // 토큰이 유효합니다.
        } catch (Exception e) {
            return false; // 토큰 검증에 실패했습니다.
        }
    }

    public boolean validateTokenIsManipulated(String token) {

        try {

            log.info(secret);

            byte[] decodedSecretKey = Base64.getDecoder().decode(secret);
            Key key = new SecretKeySpec(decodedSecretKey, 0, decodedSecretKey.length, "HmacSHA256");

            Jwts.parser()
                    .setSigningKey(key) // 비밀 키를 사용하여 서명을 검증
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException e) {
            log.info(e.getMessage() + " 토근 검증 실패");
            return false; // 토큰 검증에 실패했습니다.
        }
    }
}
