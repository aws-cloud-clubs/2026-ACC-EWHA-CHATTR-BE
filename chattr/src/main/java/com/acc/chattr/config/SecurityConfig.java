package com.acc.chattr.config;

import com.acc.chattr.security.CognitoUserSyncFilter;
import com.acc.chattr.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CognitoUserSyncFilter cognitoUserSyncFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.region}")
    private String region;

    public SecurityConfig(CognitoUserSyncFilter cognitoUserSyncFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.cognitoUserSyncFilter = cognitoUserSyncFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = String.format(
            "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
            region,
            userPoolId
        );
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                    "/auth/signup", "/auth/login", "/auth/refresh").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterAfter(cognitoUserSyncFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
