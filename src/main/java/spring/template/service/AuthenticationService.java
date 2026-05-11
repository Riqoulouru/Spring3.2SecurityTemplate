package spring.template.service;


import jakarta.servlet.http.HttpServletRequest;
import spring.template.dto.request.SignUpRequest;
import spring.template.dto.request.SigninRequest;
import spring.template.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request, HttpServletRequest httpRequest);

    JwtAuthenticationResponse signin(SigninRequest request, HttpServletRequest httpRequest);
}
