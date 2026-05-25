package com.acc.chattr.config;

import com.acc.chattr.security.CognitoUserSyncFilter;
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

    @Value("${aws.cognito.user-pool-id:ap-northeast-2_test}")
    private String userPoolId;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    public SecurityConfig(CognitoUserSyncFilter cognitoUserSyncFilter) {
        this.cognitoUserSyncFilter = cognitoUserSyncFilter;
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
                .requestMatchers("/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .addFilterAfter(cognitoUserSyncFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
