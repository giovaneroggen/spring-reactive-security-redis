package br.com.poc;

import br.com.poc.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class Application {

    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String,Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));

    @Autowired
    private SecurityService service;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> routerFunctions() {
        return RouterFunctions.route(GET("/sayHelloUser"), req -> ServerResponse.ok().body(this.service.sayHelloUser(), String.class))
                .and(RouterFunctions.route(GET("/sayHelloAdmin"), req -> ServerResponse.ok().body(this.service.sayHelloAdmin(), String.class)))
                .and(RouterFunctions.route(GET("/sayHelloAnonymous"), req -> ServerResponse.ok().body(this.service.sayHelloAnonymous(), String.class)));
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
                        .map(u -> new UsernamePasswordAuthenticationToken(u, u.getPassword(), u.getAuthorities()))
                        .map(it -> it);
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
    public ReactiveRedisTemplate<String, CustomUserDetails> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<CustomUserDetails> valueSerializer = new Jackson2JsonRedisSerializer<>(CustomUserDetails.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, CustomUserDetails> builder = RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, CustomUserDetails> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
