package com.acc.chattr.domain.user.controller;

import com.acc.chattr.common.response.PageResponse;
import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저", description = "유저 조회 및 검색 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<Response<UserResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Response.ok(userService.getMe(jwt.getSubject())));
    }

    @Operation(
        summary = "유저 목록 조회 / 검색",
        description = """
            파라미터 조합에 따라 다른 조회를 수행합니다.
            - `workspaceId` 지정: 워크스페이스 소속 유저 조회 (온라인 상태 포함)
            - `isOnline=true`: 현재 온라인 유저 목록 조회
            - `query` 지정: 유저 ID 및 닉네임으로 검색
            - 파라미터 없음: 전체 유저 목록 조회 (관리자용)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping
    public ResponseEntity<Response<PageResponse<UserResponse>>> getUsers(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID (지정 시 워크스페이스 소속 유저 조회)")
        @RequestParam(required = false) String workspaceId,
        @Parameter(description = "검색 키워드 (이메일 또는 닉네임)")
        @RequestParam(required = false) String query,
        @Parameter(description = "온라인 상태 필터 (true 지정 시 온라인 유저만 반환)")
        @RequestParam(required = false) Boolean isOnline,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        if (workspaceId != null && !workspaceId.isBlank()) {
            return ResponseEntity.ok(Response.ok(userService.getWorkspaceUsers(jwt.getSubject(), workspaceId, query, page, size)));
        }
        if (Boolean.TRUE.equals(isOnline)) {
            return ResponseEntity.ok(Response.ok(userService.getOnlineUsers(page, size)));
        }
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(Response.ok(userService.searchUsers(query, page, size)));
        }
        return ResponseEntity.ok(Response.ok(userService.getAllUsers(page, size)));
    }
}
