package br.com.poc;

import br.com.poc.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Set;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@SpringBootApplication
public class Application implements CommandLineRunner {

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
    public SecurityWebFilterChain securitygWebFilterChain(ReactiveUserDetailsService userDetailsService, ServerHttpSecurity http) {
        AuthenticationWebFilter webFilter = new AuthenticationWebFilter(this.userDetailsRepositoryReactiveAuthenticationManager(userDetailsService));
        http.addFilterAt(webFilter, SecurityWebFiltersOrder.HTTP_BASIC);
        return http
                .csrf().disable()
                .authorizeExchange().anyExchange().permitAll()
                .and().build();
    }

    @Bean
    public ReactiveAuthenticationManager userDetailsRepositoryReactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService){
        UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(this.passwordEncoder());
        return manager;
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(){
        return username -> service.findByUsername(username).map(it -> it);
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


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

    @Override
    public void run(String... args) throws Exception {
        String password = this.passwordEncoder().encode("password");
        CustomUserDetails admin = new CustomUserDetails(password,"admin", Set.of(()->"ADMIN", ()->"USER"));
        CustomUserDetails user = new CustomUserDetails(password, "user", Set.of(()->"USER"));
        this.service.save(admin);
        this.service.save(user);
    }
}
