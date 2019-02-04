package br.com.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.WebFilter;
import reactivefeign.cloud.CloudReactiveFeign;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityClientConfiguration {

    @Qualifier("securityClient")
    @Autowired
    private SecurityClient securityClient;

    @Bean
    @Qualifier("securityClient")
    public SecurityClient securityClient(){
        return CloudReactiveFeign.<SecurityClient>builder()
                .target(SecurityClient.class, SecurityClient.url());
    }

    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .addFilterAt(this.bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange().anyExchange().permitAll()
                .and().build();
    }

    private WebFilter bearerAuthenticationFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(Mono::just);
        filter.setServerAuthenticationConverter(
                exchange ->
                        Mono.justOrEmpty(exchange)
                                .flatMap(e -> Mono.justOrEmpty(e.getRequest()
                                        .getHeaders()
                                        .getFirst(HttpHeaders.AUTHORIZATION)))
                                .flatMap(it -> securityClient.principal(SecurityContract.BEARER + it))
        );
        return filter;
    }
}
