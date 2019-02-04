package br.com.security;

import br.com.security.request.LoginRequest;
import br.com.security.response.LoginResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface SecurityContract {

    public static final String BEARER = "Bearer ";

    @PostMapping(path = "/oauth/token")
    Mono<LoginResponse> login(@RequestBody Mono<LoginRequest> request);

    @GetMapping(path = "/oauth/token")
    Mono<Authentication> principal(@RequestHeader(HttpHeaders.AUTHORIZATION) String header);
}
