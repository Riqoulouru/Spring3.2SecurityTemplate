package spring.template.dto.request;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String token;
}
