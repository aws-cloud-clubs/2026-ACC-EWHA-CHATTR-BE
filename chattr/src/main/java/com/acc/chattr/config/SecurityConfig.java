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
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

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
        String issuer = String.format(
            "https://cognito-idp.%s.amazonaws.com/%s",
            region,
            userPoolId
        );
        String jwkSetUri = issuer + "/.well-known/jwks.json";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                    "/auth/signup", "/auth/login", "/auth/refresh", "/ws/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterAfter(cognitoUserSyncFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
