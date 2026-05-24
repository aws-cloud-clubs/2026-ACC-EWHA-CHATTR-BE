package com.acc.chattr.domain.file.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.file.dto.PresignRequest;
import com.acc.chattr.domain.file.dto.PresignResponse;
import com.acc.chattr.domain.file.service.FileService;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
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
    private final UserRepository userRepository;

    public FileController(FileService fileService, UserRepository userRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/presign")
    public ResponseEntity<Response<PresignResponse>> presign(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody PresignRequest request
    ) {
        String userId = userRepository.findByCognitoSub(jwt.getSubject())
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND))
            .getId();
        return ResponseEntity.ok(Response.ok(fileService.presign(userId, request)));
    }
}
