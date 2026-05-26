package com.acc.chattr.security;

import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class CognitoUserSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public CognitoUserSyncFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.warn("CognitoUserSyncFilter: URI={} auth={}", request.getRequestURI(), auth != null ? auth.getClass().getSimpleName() : "null");
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String cognitoSub = jwt.getSubject();
            log.warn("CognitoUserSyncFilter: sub={}", cognitoSub);
            try {
                userRepository.findByCognitoSub(cognitoSub).orElseGet(() -> {
                    String email = jwt.getClaimAsString("email");
                    String nickname = jwt.getClaimAsString("nickname");
                    log.info("CognitoUserSyncFilter: creating user email={} nickname={}", email, nickname);
                    if (nickname == null || nickname.isBlank()) {
                        nickname = email.split("@")[0];
                    }
                    User newUser = User.create(UUID.randomUUID().toString(), email, nickname, cognitoSub);
                    userRepository.save(newUser);
                    log.info("CognitoUserSyncFilter: user created id={}", newUser.getId());
                    return newUser;
                });
            } catch (Exception e) {
                log.error("CognitoUserSyncFilter: failed to sync user sub={}", cognitoSub, e);
            }
        }
        chain.doFilter(request, response);
    }
}
