package spring.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.template.dto.request.PasswordResetConfirmRequest;
import spring.template.dto.request.PasswordResetRequest;
import spring.template.dto.response.DevelopmentTokenResponse;
import spring.template.dto.response.MessageResponse;
import spring.template.security.features.passwordreset.PasswordResetService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security.features.password-reset", name = "enabled", havingValue = "true")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/password-reset/request")
    public ResponseEntity<DevelopmentTokenResponse> requestReset(@RequestBody PasswordResetRequest request) {
        String token = passwordResetService.requestReset(request.getEmail()).orElse(null);
        return ResponseEntity.accepted().body(DevelopmentTokenResponse.builder()
                .message("If the email exists, a password reset token has been generated")
                .token(token)
                .build());
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<MessageResponse> confirmReset(@RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder().message("Password updated").build());
    }
}
