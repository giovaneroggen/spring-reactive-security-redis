package br.com.poc;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserDetailsRepository extends ReactiveMongoRepository<CustomUserDetails, String> {

    Mono<CustomUserDetails> findFirstByUsername(String username);
}
