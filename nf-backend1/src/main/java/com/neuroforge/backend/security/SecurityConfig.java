package com.neuroforge.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    private static final String[] PUBLIC_PATHS = {
        "/auth/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",
        "/v3/api-docs/**",
        "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Super admin only
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_SUPER_ADMIN")

                // Org management
                .requestMatchers("/api/organizations/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN")

                // Project management — PM, Org Admin, Super Admin
                .requestMatchers("/api/projects/**").hasAnyAuthority(
                        "ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROJECT_MANAGER")

                // Sprint management
                .requestMatchers("/api/sprints/**").hasAnyAuthority(
                        "ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROJECT_MANAGER")

                // Task management — extended to developers and testers
                .requestMatchers("/api/tasks/**").hasAnyAuthority(
                        "ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROJECT_MANAGER",
                        "ROLE_DEVELOPER", "ROLE_TESTER")

                // Project member assignment
                .requestMatchers("/api/project-members/**").hasAnyAuthority(
                        "ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN", "ROLE_PROJECT_MANAGER")

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
