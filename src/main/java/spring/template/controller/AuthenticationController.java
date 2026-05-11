package spring.template.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import spring.template.dto.request.SignUpRequest;
import spring.template.dto.request.SigninRequest;
import spring.template.dto.response.JwtAuthenticationResponse;
import spring.template.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@RequestBody SignUpRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authenticationService.signup(request, httpRequest));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authenticationService.signin(request, httpRequest));
    }
}
