package com.acc.chattr.domain.auth.dto;

public record TokenResponse(
    String idToken,
    String accessToken,
    String refreshToken,
    String username,
    int expiresIn
) {}
