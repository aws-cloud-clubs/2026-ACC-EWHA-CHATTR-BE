package com.acc.chattr.security;

import com.acc.chattr.common.code.GeneralErrorCode;
import com.acc.chattr.common.exception.GeneralException;
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
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String cognitoSub = jwt.getSubject();
            try {
                userRepository.findByCognitoSub(cognitoSub).orElseGet(() -> {
                    String email = jwt.getClaimAsString("email");
                    if (email == null || email.isBlank()) {
                        throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
                    }
                    String nickname = jwt.getClaimAsString("nickname");
                    if (nickname == null || nickname.isBlank()) {
                        nickname = email.split("@")[0];
                    }
                    log.debug("CognitoUserSyncFilter: 신규 유저 생성 sub={}", cognitoSub);
                    User newUser = User.create(UUID.randomUUID().toString(), email, nickname, cognitoSub);
                    userRepository.save(newUser);
                    log.debug("CognitoUserSyncFilter: 유저 생성 완료 id={}", newUser.getId());
                    return newUser;
                });
            } catch (GeneralException e) {
                log.warn("CognitoUserSyncFilter: 유저 동기화 거부 sub={} code={}", cognitoSub, e.getErrorCode());
                response.sendError(e.getErrorCode().getStatusCode(), e.getErrorCode().getMessage());
                return;
            } catch (Exception e) {
                log.error("CognitoUserSyncFilter: 유저 동기화 실패 sub={}", cognitoSub, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "유저 동기화에 실패했습니다.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
