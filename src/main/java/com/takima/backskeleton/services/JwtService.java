package com.takima.backskeleton.services;

import com.takima.backskeleton.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_TYPE, accessTokenExpirationMs, true);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs, false);
    }

    public boolean validateAccessToken(String token) {
        return isValidTokenOfType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return isValidTokenOfType(token, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(User user, String tokenType, long expirationMs, boolean includeRole) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur doit être enregistré avant de générer un token");
        }

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);

        var builder = Jwts.builder()
                .setSubject(user.getId().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration);

        if (includeRole && user.getRole() != null) {
            builder.claim("role", user.getRole().getRoleName());
        }

        return builder
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isValidTokenOfType(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return expectedType.equals(parsed.getBody().get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
