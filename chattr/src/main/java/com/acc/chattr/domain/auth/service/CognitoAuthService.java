package com.acc.chattr.domain.auth.service;

import com.acc.chattr.domain.auth.dto.ConfirmRequest;
import com.acc.chattr.domain.auth.dto.LoginRequest;
import com.acc.chattr.domain.auth.dto.RefreshRequest;
import com.acc.chattr.domain.auth.dto.SignupRequest;
import com.acc.chattr.domain.auth.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class CognitoAuthService {

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.cognito.client-secret}")
    private String clientSecret;

    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoAuthService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void signup(SignupRequest request) {
        cognitoClient.signUp(r -> r
            .clientId(clientId)
            .secretHash(secretHash(request.email()))
            .username(request.email())
            .password(request.password())
            .userAttributes(
                AttributeType.builder().name("email").value(request.email()).build(),
                AttributeType.builder().name("nickname").value(request.nickname()).build()
            )
        );
    }

    public void confirm(ConfirmRequest request) {
        cognitoClient.confirmSignUp(ConfirmSignUpRequest.builder()
            .clientId(clientId)
            .secretHash(secretHash(request.email()))
            .username(request.email())
            .confirmationCode(request.code())
            .build()
        );
    }

    public TokenResponse login(LoginRequest request) {
        InitiateAuthResponse response = cognitoClient.initiateAuth(
            InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                    "USERNAME", request.email(),
                    "PASSWORD", request.password(),
                    "SECRET_HASH", secretHash(request.email())
                ))
                .build()
        );
        return toTokenResponse(response.authenticationResult(), request.email());
    }

    public TokenResponse refresh(RefreshRequest request) {
        InitiateAuthResponse response = cognitoClient.initiateAuth(
            InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                    "REFRESH_TOKEN", request.refreshToken(),
                    "SECRET_HASH", secretHash(request.username())
                ))
                .build()
        );
        AuthenticationResultType result = response.authenticationResult();
        // 리프레시 응답엔 refresh_token이 포함되지 않음 — 기존 토큰을 그대로 사용
        return new TokenResponse(result.idToken(), result.accessToken(), null, request.username(), result.expiresIn());
    }

    private TokenResponse toTokenResponse(AuthenticationResultType result, String username) {
        return new TokenResponse(
            result.idToken(),
            result.accessToken(),
            result.refreshToken(),
            username,
            result.expiresIn()
        );
    }

    private String secretHash(String username) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((username + clientId).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SECRET_HASH 생성 실패", e);
        }
    }
}
