package br.com.poc.service;

import br.com.poc.CustomUserDetails;
import br.com.poc.UserDetailsRepository;
import br.com.poc.request.LoginRequest;
import br.com.poc.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityService {

    @Autowired
    private ReactiveRedisTemplate<String, CustomUserDetails> template;

    @Autowired
    private UserDetailsRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> sayHelloUser(){
        return Mono.just("OLA_USER");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<String> sayHelloAdmin(){
        return Mono.just("OLA_USER");
    }

    @PreAuthorize("isAnonymous()")
    public Mono<String> sayHelloAnonymous(){
        return Mono.just("OLA_ANONYMOUS");
    }

    public Mono<CustomUserDetails> findByToken(String token) {
        return template.opsForValue().get(token);
    }

    public void save(CustomUserDetails details) {
        details.setPassword(this.passwordEncoder.encode(details.getPassword()));
        this.repository.save(details).subscribe();
    }

    public Mono<LoginResponse> login(Mono<LoginRequest> request) {
        String token = UUID.randomUUID().toString();
        return request.flatMap(it ->
                this.repository
                    .findFirstByUsername(it.getUsername())
                    .filter(u -> this.passwordEncoder.matches(it.getPassword(), u.getPassword()))
                    .flatMap(u -> template.opsForValue().set(token, u))
                    .filter(bool -> bool)
                    .map(bool -> LoginResponse.builder()
                                        .token(token)
                                        .build())
                    .switchIfEmpty(Mono.error(() -> new BadCredentialsException("NO USER FOUND")))
        );
    }
}