package com.medilynk.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {
    public  final Key secretKey;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

    }

    public  String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
                .signWith(secretKey)
                .compact();
    }

    public  void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey)  secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SignatureException ex) {
            throw new JwtException("Invalid JWT token");
        } catch (JwtException ex) {
            throw new JwtException("Expired or invalid JWT token");
        }
    }
}
