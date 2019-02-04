package br.com.poc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RestControllerAdvice
public class BadCredentialsExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<Void> genericExceptionHandler(BadCredentialsException error) {
        log.error("BadCredentialsExceptionHandler.genericExceptionHandler={}", error);
        return Mono.empty();
    }

}
