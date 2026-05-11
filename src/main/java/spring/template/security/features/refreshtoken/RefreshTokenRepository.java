package spring.template.security.features.refreshtoken;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.template.entities.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteByUser(User user);
}
