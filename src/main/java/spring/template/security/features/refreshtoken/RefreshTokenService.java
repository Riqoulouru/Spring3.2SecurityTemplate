package spring.template.security.features.refreshtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import spring.template.entities.User;
import spring.template.security.features.common.SecureTokenService;
import spring.template.service.JwtService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureTokenService secureTokenService;
    private final JwtService jwtService;

    @Value("${security.features.refresh-token.enabled:false}")
    private boolean enabled;

    @Value("${security.features.refresh-token.expiration-ms:604800000}")
    private long expirationMs;

    @Transactional
    public String create(User user) {
        if (!enabled) {
            return null;
        }
        String token = secureTokenService.generateToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(secureTokenService.hash(token))
                .user(user)
                .expiresAt(Instant.now().plusMillis(expirationMs))
                .revoked(false)
                .build());
        return token;
    }

    @Transactional
    public RefreshTokenResult rotate(String refreshToken) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token feature is disabled");
        }
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndRevokedFalse(secureTokenService.hash(refreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired refresh token");
        }
        storedToken.setRevoked(true);
        User user = storedToken.getUser();
        String accessToken = jwtService.generateToken(user);
        String newRefreshToken = create(user);
        return new RefreshTokenResult(accessToken, newRefreshToken);
    }

    @Transactional
    public void revokeAll(User user) {
        if (enabled) {
            refreshTokenRepository.deleteByUser(user);
        }
    }

    public record RefreshTokenResult(String accessToken, String refreshToken) {
    }
}
