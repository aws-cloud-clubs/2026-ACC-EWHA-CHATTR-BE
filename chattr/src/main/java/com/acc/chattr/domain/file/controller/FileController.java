package com.acc.chattr.domain.file.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.file.dto.PresignRequest;
import com.acc.chattr.domain.file.dto.PresignResponse;
import com.acc.chattr.domain.file.service.FileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/presign")
    public ResponseEntity<Response<PresignResponse>> presign(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid PresignRequest request
    ) {
        return ResponseEntity.ok(Response.ok(fileService.presign(jwt.getSubject(), request)));
    }
}
