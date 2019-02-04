package br.com.security;

import br.com.security.data.CustomUserDetails;
import br.com.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@SpringBootApplication
public class SecurityApplication implements CommandLineRunner {

    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String,Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));

    @Autowired
    private SecurityService service;

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }


    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/oauth/token")
                .permitAll()
                .and()
                .addFilterAt(this.bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange().anyExchange().permitAll()
                .and().build();
    }

    private AuthenticationWebFilter bearerAuthenticationFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(this.authenticationManager());
        filter.setServerAuthenticationConverter(
                exchange ->
                        Mono.justOrEmpty(exchange)
                                .flatMap(e -> Mono.justOrEmpty(e.getRequest()
                                        .getHeaders()
                                        .getFirst(HttpHeaders.AUTHORIZATION)))
                                .filter(matchBearerLength)
                                .flatMap(isolateBearerValue)
                                .map(u -> new UsernamePasswordAuthenticationToken(u, null, AuthorityUtils.NO_AUTHORITIES))
        );
        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(){
        return a -> this.service
                .findByToken(a.getName())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Invalid Credentials"))))
                .map(u -> new UsernamePasswordAuthenticationToken(u, u.getPassword(), u.getAuthorities()));
    }

    /*
     * REDIS
     */
    @Bean
    public LettuceConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public ReactiveRedisConnection reactiveRedisConnection(final LettuceConnectionFactory redisConnectionFactory) {
        return redisConnectionFactory.getReactiveConnection();
    }

    @Bean
    public ReactiveRedisTemplate<String, CustomUserDetails> reactiveRedisTemplate(LettuceConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<CustomUserDetails> valueSerializer = new Jackson2JsonRedisSerializer<>(CustomUserDetails.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, CustomUserDetails> builder = RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, CustomUserDetails> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        this.service.save(new CustomUserDetails("password", "admin", Set.of(new SimpleGrantedAuthority("ADMIN"), new SimpleGrantedAuthority("USER"))));
        this.service.save(new CustomUserDetails("password", "user", Set.of(new SimpleGrantedAuthority("USER"))));
    }
}
