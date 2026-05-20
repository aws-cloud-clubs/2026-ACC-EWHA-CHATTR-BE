package com.acc.chattr.domain.health.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.health.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Response<HealthResponse>> health() {
        return ResponseEntity.ok(Response.ok(HealthResponse.up()));
    }
}
