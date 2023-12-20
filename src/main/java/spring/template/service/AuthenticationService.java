package spring.template.service;


import spring.template.dao.request.SignUpRequest;
import spring.template.dao.request.SigninRequest;
import spring.template.dao.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signup(SignUpRequest request);

    JwtAuthenticationResponse signin(SigninRequest request);
}
