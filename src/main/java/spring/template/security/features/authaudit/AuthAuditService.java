package spring.template.security.features.authaudit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthAuditService {
    private final AuthAuditLogRepository authAuditLogRepository;

    @Value("${security.features.audit-log.enabled:false}")
    private boolean enabled;

    public void record(String eventType, String email, boolean success, String message, HttpServletRequest request) {
        if (!enabled) {
            return;
        }
        authAuditLogRepository.save(AuthAuditLog.builder()
                .eventType(eventType)
                .email(email)
                .success(success)
                .message(message)
                .ipAddress(resolveIpAddress(request))
                .userAgent(request == null ? null : request.getHeader("User-Agent"))
                .createdAt(Instant.now())
                .build());
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
