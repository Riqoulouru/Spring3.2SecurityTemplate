package spring.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.template.dto.request.EmailVerificationRequest;
import spring.template.dto.response.MessageResponse;
import spring.template.security.features.emailverification.EmailVerificationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security.features.email-verification", name = "enabled", havingValue = "true")
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        emailVerificationService.verify(request.getToken());
        return ResponseEntity.ok(MessageResponse.builder().message("Email verified").build());
    }
}
