package br.com.security.data.repository;

import br.com.security.data.CustomUserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserDetailsRepository extends ReactiveMongoRepository<CustomUserDetails, String> {

    Mono<CustomUserDetails> findFirstByUsername(String username);
}
