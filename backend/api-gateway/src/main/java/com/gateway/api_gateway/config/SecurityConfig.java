package com.gateway.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;

import java.util.List;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {




    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http

                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/invite",
                                "/auth/forgot-password/**",
                                "/auth/reset-password",
                                "/auth/send-otp",
                                "/auth/verify-otp"
                        ).permitAll()

                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .pathMatchers("/user/**").hasAnyRole("USER", "ADMIN")
//                        .pathMatchers("/restaurant/**")
//                        .hasAnyRole("RESTAURANT", "ADMIN")
                        .anyExchange().authenticated()
                )
                .build();
    }
}
