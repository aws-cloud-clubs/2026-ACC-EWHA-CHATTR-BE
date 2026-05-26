package com.acc.chattr.domain.file.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.file.dto.PresignRequest;
import com.acc.chattr.domain.file.dto.PresignResponse;
import com.acc.chattr.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일", description = "S3 Presigned URL 발급 API")
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
        summary = "Presigned URL 발급",
        description = "S3에 파일을 직접 업로드하기 위한 Presigned PUT URL을 발급합니다. " +
            "응답의 `url`로 PUT 요청을 보내고, `fileKey`를 메시지 등에서 파일 참조로 사용하세요."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL 발급 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/presign")
    public ResponseEntity<Response<PresignResponse>> presign(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid PresignRequest request
    ) {
        return ResponseEntity.ok(Response.ok(fileService.presign(jwt.getSubject(), request)));
    }
}
