package com.example.template.auth.service;

import com.example.template.auth.dto.*;
import com.example.template.email.EmailService;
import com.example.template.exception.ApiException;
import com.example.template.security.JwtService;
import com.example.template.user.entity.*;
import com.example.template.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.email.verification-token-expiry-ms}")
    private long verificationTokenExpiryMs;

    @Value("${app.email.password-reset-token-expiry-ms}")
    private long passwordResetTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default role ROLE_USER not found — check Flyway migration V3"));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .build();
        user.getRoles().add(userRole);
        userRepository.save(user);

        String tokenValue = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(verificationTokenExpiryMs))
                .build());

        emailService.sendVerificationEmail(email, tokenValue);
    }

    @Transactional
    public void confirmEmail(String tokenValue) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid confirmation token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This confirmation link has already been used");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This confirmation link has expired. Please register again.");
        }

        token.getUser().setEnabled(true);
        token.setUsedAt(Instant.now());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Please confirm your email address before logging in");
        } catch (BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);

        String refreshTokenValue = UUID.randomUUID().toString();
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid refresh token"));

        if (token.getRevokedAt() != null) {
            // Already revoked — treat as success (idempotent)
            return;
        }
        token.setRevokedAt(Instant.now());
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.getRevokedAt() != null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getEmail());
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())  // reuse same refresh token
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return the same response to prevent email enumeration attacks
        userRepository.findByEmail(request.getEmail().toLowerCase().trim()).ifPresent(user -> {
            String tokenValue = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(passwordResetTokenExpiryMs))
                    .build());
            emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));

        if (token.getUsedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This password reset link has already been used");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This password reset link has expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        token.setUsedAt(Instant.now());

        // Revoke all active refresh tokens so any existing sessions are invalidated
        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
    }
}
