package spring.template.security.features.emailverification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import spring.template.entities.User;
import spring.template.repository.UserRepository;
import spring.template.security.features.common.SecureTokenService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecureTokenService secureTokenService;

    @Value("${security.features.email-verification.enabled:false}")
    private boolean enabled;

    @Value("${security.features.email-verification.expiration-ms:86400000}")
    private long expirationMs;

    public boolean isEnabled() {
        return enabled;
    }

    public String createToken(User user) {
        if (!enabled) {
            return null;
        }
        String token = secureTokenService.generateToken();
        tokenRepository.save(EmailVerificationToken.builder()
                .tokenHash(secureTokenService.hash(token))
                .user(user)
                .expiresAt(Instant.now().plusMillis(expirationMs))
                .used(false)
                .build());
        return token;
    }

    @Transactional
    public void verify(String token) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email verification feature is disabled");
        }
        EmailVerificationToken storedToken = tokenRepository.findByTokenHashAndUsedFalse(secureTokenService.hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired verification token");
        }
        User user = storedToken.getUser();
        user.setEmailVerified(true);
        storedToken.setUsed(true);
        userRepository.save(user);
    }
}
