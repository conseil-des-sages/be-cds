package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.RefreshTokenDao;
import com.takima.backskeleton.models.RefreshToken;
import com.takima.backskeleton.models.User;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Service
public class RefreshTokenService {
    private final RefreshTokenDao refreshTokenDao;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenDao refreshTokenDao, JwtService jwtService) {
        this.refreshTokenDao = refreshTokenDao;
        this.jwtService = jwtService;
    }

    /** Issues an access token and a refresh token, storing only the refresh token's SHA-256 hash. */
    @Transactional
    public IssuedTokens issueTokens(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        storeRefreshToken(user, refreshToken);
        String accessToken = jwtService.generateAccessToken(user);
        return new IssuedTokens(accessToken, refreshToken);
    }

    /** Rotates a refresh token once; the row lock prevents two simultaneous uses from both succeeding. */
    @Transactional
    public IssuedTokens rotateRefreshToken(String rawRefreshToken) {
        if (!jwtService.validateRefreshToken(rawRefreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        RefreshToken storedToken = refreshTokenDao.findByTokenHashForUpdate(hashToken(rawRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token révoqué ou inconnu"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (storedToken.isRevoked() || !storedToken.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("Refresh token révoqué ou expiré");
        }

        Integer tokenUserId;
        try {
            tokenUserId = jwtService.getUserIdFromRefreshToken(rawRefreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Refresh token invalide", exception);
        }
        User user = storedToken.getUser();
        if (!user.getId().equals(tokenUserId)) {
            throw new IllegalArgumentException("Le refresh token ne correspond pas à son utilisateur");
        }

        storedToken.setRevoked(true);
        refreshTokenDao.save(storedToken);

        String nextRefreshToken = jwtService.generateRefreshToken(user);
        storeRefreshToken(user, nextRefreshToken);
        String accessToken = jwtService.generateAccessToken(user);
        return new IssuedTokens(accessToken, nextRefreshToken);
    }

    /** Revokes a refresh token, for example when the user logs out. */
    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        refreshTokenDao.findByTokenHashForUpdate(hashToken(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenDao.save(token);
                });
    }

    private void storeRefreshToken(User user, String rawRefreshToken) {
        RefreshToken token = new RefreshToken();
        token.setTokenHash(hashToken(rawRefreshToken));
        token.setExpiresAt(LocalDateTime.ofInstant(
                jwtService.getExpirationFromRefreshToken(rawRefreshToken).toInstant(), ZoneOffset.UTC));
        token.setRevoked(false);
        token.setUser(user);
        refreshTokenDao.save(token);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 n'est pas disponible", exception);
        }
    }

    public record IssuedTokens(String accessToken, String refreshToken) {
    }
}
