package com.acc.chattr.domain.auth.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.auth.dto.DeviceResponse;
import com.acc.chattr.domain.auth.dto.RegisterDeviceRequest;
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

@Tag(name = "인증", description = "로그인, 토큰 갱신, 디바이스 세션 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final DeviceService deviceService;

    public AuthController(DeviceService deviceService) {
        this.deviceService = deviceService;
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
    @GetMapping("/devices")
    public ResponseEntity<Response<List<DeviceResponse>>> getDevices(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(Response.ok(deviceService.getDevices(jwt.getSubject())));
    }
}
