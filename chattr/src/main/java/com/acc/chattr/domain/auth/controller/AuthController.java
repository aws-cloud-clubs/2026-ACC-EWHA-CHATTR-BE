package com.acc.chattr.domain.auth.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.auth.dto.DeviceResponse;
import com.acc.chattr.domain.auth.dto.RegisterDeviceRequest;
import com.acc.chattr.domain.auth.service.DeviceService;
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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final DeviceService deviceService;

    public AuthController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/device/register")
    public ResponseEntity<Response<DeviceResponse>> registerDevice(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid RegisterDeviceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response.ok(deviceService.register(jwt.getSubject(), request)));
    }

    @GetMapping("/devices")
    public ResponseEntity<Response<List<DeviceResponse>>> getDevices(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(Response.ok(deviceService.getDevices(jwt.getSubject())));
    }
}
