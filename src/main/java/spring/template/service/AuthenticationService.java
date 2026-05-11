package spring.template.service;


import spring.template.dto.request.SignUpRequest;
import spring.template.dto.request.SigninRequest;
import spring.template.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request);

    JwtAuthenticationResponse signin(SigninRequest request);
}
