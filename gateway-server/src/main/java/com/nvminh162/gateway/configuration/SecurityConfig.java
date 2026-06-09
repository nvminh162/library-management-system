package com.nvminh162.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

        @Bean
        @Primary
        public KeyResolver userKeyResolver() {
                return exchange -> exchange.getPrincipal()
                                .map(java.security.Principal::getName)
                                .defaultIfEmpty(exchange.getRequest().getRemoteAddress() != null
                                                && exchange.getRequest().getRemoteAddress().getAddress() != null
                                                                ? exchange.getRequest().getRemoteAddress().getAddress()
                                                                                .getHostAddress()
                                                                : "anonymous");
        }

        @Bean
        SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
                http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchange -> exchange
                                                .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                                                .pathMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                                                .pathMatchers(HttpMethod.GET, "/api/v1/employees/**").permitAll()
                                                .pathMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
                return http.build();
        }
}
