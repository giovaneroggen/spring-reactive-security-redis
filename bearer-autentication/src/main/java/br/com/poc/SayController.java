package br.com.poc;

import br.com.poc.request.LoginRequest;
import br.com.poc.response.LoginResponse;
import br.com.poc.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@RestController
public class SayController {

    @Autowired
    private SecurityService service;

    @GetMapping(path = "/sayHelloUser")
    Mono<String> sayHelloUser(){
        return this.service.sayHelloUser();
    }

    @GetMapping(path = "/sayHelloAdmin")
    Mono<String> sayHelloAdmin(){
        return this.service.sayHelloAdmin();
    }

    @GetMapping(path = "/sayHelloAnonymous")
    Mono<String> sayHelloAnonymous(){
        return this.service.sayHelloAnonymous();
    }

    @GetMapping(path = "/principal")
    Mono<Principal> principal(@AuthenticationPrincipal Mono<Principal> principal){
        return principal;
    }

    @PostMapping(path = "/oauth/token")
    Mono<LoginResponse> login(@RequestBody Mono<LoginRequest> request){
        return service.login(request);
    }

}
