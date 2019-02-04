package br.com.security.web;

import br.com.security.SecurityContract;
import br.com.security.request.LoginRequest;
import br.com.security.response.LoginResponse;
import br.com.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SecurityController implements SecurityContract {

    @Autowired
    private SecurityService service;

    @Override
    public Mono<LoginResponse> login(Mono<LoginRequest> request) {
        return this.service.login(request);
    }

    @Override
    public Mono<Authentication> principal(String header) {
        return Mono.just(SecurityContextHolder.getContext().getAuthentication());
    }
}
