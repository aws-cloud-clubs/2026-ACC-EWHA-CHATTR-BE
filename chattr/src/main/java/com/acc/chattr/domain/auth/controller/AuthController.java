package com.acc.chattr.domain.auth.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.auth.dto.DeviceResponse;
import com.acc.chattr.domain.auth.dto.LoginRequest;
import com.acc.chattr.domain.auth.dto.RefreshRequest;
import com.acc.chattr.domain.auth.dto.RegisterDeviceRequest;
import com.acc.chattr.domain.auth.dto.SignupRequest;
import com.acc.chattr.domain.auth.dto.TokenResponse;
import com.acc.chattr.domain.auth.service.CognitoAuthService;
import com.acc.chattr.domain.auth.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 갱신, 디바이스 세션 관련 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 디바이스/로그아웃 엔드포인트는 JWT 인증 필요 (SecurityConfig 참조)

    private final CognitoAuthService cognitoAuthService;
    private final DeviceService deviceService;

    public AuthController(CognitoAuthService cognitoAuthService, DeviceService deviceService) {
        this.cognitoAuthService = cognitoAuthService;
        this.deviceService = deviceService;
    }

    @Operation(summary = "회원가입", description = "Cognito에 계정을 생성하고 즉시 활성화합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<Response<Void>> signup(@RequestBody @Valid SignupRequest request) {
        cognitoAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인합니다. 반환된 idToken을 Authorization 헤더에 사용하세요.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<Response<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(Response.ok(cognitoAuthService.login(request)));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access/ID Token을 발급합니다. username은 로그인 응답의 username 값입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Response<TokenResponse>> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(Response.ok(cognitoAuthService.refresh(request)));
    }

    @Operation(
        summary = "디바이스 세션 발급",
        description = "멀티 디바이스 로그인을 위한 디바이스 세션 정보를 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "디바이스 등록 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/device/register")
    public ResponseEntity<Response<DeviceResponse>> registerDevice(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid RegisterDeviceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response.ok(deviceService.register(jwt.getSubject(), request)));
    }

    @Operation(
        summary = "로그인한 기기 목록",
        description = "현재 계정으로 로그인된 디바이스 목록 및 마지막 접속 시간을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/devices")
    public ResponseEntity<Response<List<DeviceResponse>>> getDevices(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(Response.ok(deviceService.getDevices(jwt.getSubject())));
    }

    @Operation(
        summary = "로그아웃 (모든 기기)",
        description = "Cognito Refresh Token을 전체 무효화하고 모든 디바이스 세션을 삭제합니다. " +
            "이미 발급된 ID/Access Token은 만료 시까지 유효합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        cognitoAuthService.globalSignOut(email);
        deviceService.logout(jwt.getSubject());
        return ResponseEntity.ok(Response.ok());
    }
}
