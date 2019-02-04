package br.com.gateway.web;

import br.com.security.SecurityClient;
import br.com.security.request.LoginRequest;
import br.com.security.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    @Qualifier("securityClient")
    private SecurityClient client;

    @PostMapping(path = "/oauth/token")
    Mono<LoginResponse> login(@RequestBody Mono<LoginRequest> request){
        return this.client.login(request);
    }

    @PreAuthorize("isAuthenticated() && hasAuthority('ADMIN')")
    @GetMapping(path = "/oauth/admin")
    Mono<String> admin(){
        return Mono.just("OLA ADMIN");
    }

    @PreAuthorize("isAuthenticated() && hasAuthority('USER')")
    @GetMapping(path = "/oauth/user")
    Mono<String> user(){
        return Mono.just("OLA USER");
    }


    @GetMapping(path = "/oauth/token")
    Mono<Principal> principal(@AuthenticationPrincipal Mono<Principal> principal){
        return principal;
    }
}
