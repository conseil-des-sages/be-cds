package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.RoleDao;
import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.AuthResponse;
import com.takima.backskeleton.DTO.LoginRequest;
import com.takima.backskeleton.DTO.RefreshRequest;
import com.takima.backskeleton.DTO.RegisterDto;
import com.takima.backskeleton.models.Role;
import com.takima.backskeleton.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserDao userDao,
            RoleDao roleDao,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterDto request) {
        String email = normalizeEmail(request.getEmail());
        if (userDao.existsByEmailUser(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de créer le compte avec ces informations");
        }

        Role userRole = roleDao.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Le rôle USER n'existe pas en base"));

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmailUser(email);
        user.setPasswordUser(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);

        User savedUser = userDao.save(user);
        return toAuthResponse(refreshTokenService.issueTokens(savedUser));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userDao.findByEmailUser(email).orElse(null);

        // Même réponse que l'email soit inconnu ou que le mot de passe soit incorrect.
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordUser())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }

        return toAuthResponse(refreshTokenService.issueTokens(user));
    }

    public AuthResponse refresh(RefreshRequest request) {
        try {
            return toAuthResponse(refreshTokenService.rotateRefreshToken(request.getRefreshToken()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide ou expiré");
        }
    }

    public void logout(RefreshRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
    }

    private AuthResponse toAuthResponse(RefreshTokenService.IssuedTokens tokens) {
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
