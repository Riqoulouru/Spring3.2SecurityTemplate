package spring.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.template.dto.request.RefreshTokenRequest;
import spring.template.dto.response.JwtAuthenticationResponse;
import spring.template.security.features.refreshtoken.RefreshTokenService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security.features.refresh-token", name = "enabled", havingValue = "true")
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) {
        var result = refreshTokenService.rotate(request.getRefreshToken());
        return ResponseEntity.ok(JwtAuthenticationResponse.builder()
                .token(result.accessToken())
                .refreshToken(result.refreshToken())
                .build());
    }
}
