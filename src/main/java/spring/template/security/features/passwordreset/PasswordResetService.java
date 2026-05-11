package spring.template.security.features.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import spring.template.entities.User;
import spring.template.repository.UserRepository;
import spring.template.security.features.common.SecureTokenService;
import spring.template.security.features.refreshtoken.RefreshTokenService;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenService secureTokenService;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.features.password-reset.enabled:false}")
    private boolean enabled;

    @Value("${security.features.password-reset.expiration-ms:900000}")
    private long expirationMs;

    @Transactional
    public Optional<String> requestReset(String email) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Password reset feature is disabled");
        }
        return userRepository.findByEmail(email).map(user -> {
            String token = secureTokenService.generateToken();
            tokenRepository.save(PasswordResetToken.builder()
                    .tokenHash(secureTokenService.hash(token))
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(expirationMs))
                    .used(false)
                    .build());
            return token;
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Password reset feature is disabled");
        }
        PasswordResetToken storedToken = tokenRepository.findByTokenHashAndUsedFalse(secureTokenService.hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired password reset token");
        }
        User user = storedToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        storedToken.setUsed(true);
        userRepository.save(user);
        refreshTokenService.revokeAll(user);
    }
}
