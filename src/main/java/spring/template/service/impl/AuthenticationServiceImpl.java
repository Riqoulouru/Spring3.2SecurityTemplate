package spring.template.service.impl;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import spring.template.dto.request.SignUpRequest;
import spring.template.dto.request.SigninRequest;
import spring.template.dto.response.JwtAuthenticationResponse;
import spring.template.entities.Role;
import spring.template.entities.User;
import spring.template.repository.UserRepository;
import spring.template.security.features.authaudit.AuthAuditService;
import spring.template.security.features.emailverification.EmailVerificationService;
import spring.template.security.features.ratelimit.RateLimitService;
import spring.template.security.features.refreshtoken.RefreshTokenService;
import spring.template.service.AuthenticationService;
import spring.template.service.JwtService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final AuthAuditService authAuditService;
    private final RateLimitService rateLimitService;

    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request, HttpServletRequest httpRequest) {
        rateLimitService.check("signup:" + request.getEmail());
        var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).emailVerified(!emailVerificationService.isEnabled()).build();
        userRepository.save(user);
        authAuditService.record("SIGNUP", request.getEmail(), true, "User registered", httpRequest);
        String emailVerificationToken = emailVerificationService.createToken(user);
        if (emailVerificationToken != null) {
            return JwtAuthenticationResponse.builder()
                    .emailVerificationToken(emailVerificationToken)
                    .message("Email verification required")
                    .build();
        }
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .refreshToken(refreshTokenService.create(user))
                .build();
    }

    @Override
    public JwtAuthenticationResponse signin(SigninRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey = "signin:" + request.getEmail();
        rateLimitService.check(rateLimitKey);
        User user;
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Invalid email or password."));
        } catch (RuntimeException exception) {
            authAuditService.record("SIGNIN", request.getEmail(), false, exception.getMessage(), httpRequest);
            throw exception;
        }
        rateLimitService.reset(rateLimitKey);
        authAuditService.record("SIGNIN", request.getEmail(), true, "User signed in", httpRequest);
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder()
                .token(jwt)
                .refreshToken(refreshTokenService.create(user))
                .build();
    }
}
