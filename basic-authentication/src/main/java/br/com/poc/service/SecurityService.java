package br.com.poc.service;

import br.com.poc.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SecurityService {

    @Autowired
    private ReactiveRedisTemplate<String, CustomUserDetails> template;

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

    public Mono<CustomUserDetails> findByUsername(String username) {
        return template.opsForValue().get(username);
    }

    public void save(CustomUserDetails details) {
        this.template
            .opsForValue()
            .set(details.getUsername(), details)
            .subscribe();
    }
}
