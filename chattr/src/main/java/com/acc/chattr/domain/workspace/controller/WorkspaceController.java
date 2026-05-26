package com.acc.chattr.domain.workspace.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.workspace.dto.ChangeRoleRequest;
import com.acc.chattr.domain.workspace.dto.InviteRequest;
import com.acc.chattr.domain.workspace.dto.WorkspaceCreateRequest;
import com.acc.chattr.domain.workspace.dto.WorkspaceMemberResponse;
import com.acc.chattr.domain.workspace.dto.WorkspaceResponse;
import com.acc.chattr.domain.workspace.dto.WorkspaceUpdateRequest;
import com.acc.chattr.domain.channel.dto.ChannelCreateRequest;
import com.acc.chattr.domain.channel.dto.ChannelResponse;
import com.acc.chattr.domain.channel.service.ChannelService;
import com.acc.chattr.domain.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "워크스페이스", description = "워크스페이스 생성·조회·수정·삭제 및 멤버 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ChannelService channelService;

    public WorkspaceController(WorkspaceService workspaceService, ChannelService channelService) {
        this.workspaceService = workspaceService;
        this.channelService = channelService;
    }

    @Operation(
        summary = "워크스페이스 생성",
        description = "새로운 워크스페이스를 생성합니다. 생성자는 ADMIN role을 가집니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<Response<WorkspaceResponse>> create(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid WorkspaceCreateRequest request
    ) {
        WorkspaceResponse response = workspaceService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok(response));
    }

    @Operation(
        summary = "내 워크스페이스 목록 조회",
        description = "현재 사용자가 참여 중인 워크스페이스 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<Response<List<WorkspaceResponse>>> getMyWorkspaces(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getMyWorkspaces(jwt.getSubject())));
    }

    @Operation(
        summary = "워크스페이스 단건 조회",
        description = "특정 워크스페이스의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @GetMapping("/{workspaceId}")
    public ResponseEntity<Response<WorkspaceResponse>> getWorkspace(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getWorkspace(jwt.getSubject(), workspaceId)));
    }

    @Operation(
        summary = "워크스페이스 수정",
        description = "워크스페이스 이름 및 설정 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @PatchMapping("/{workspaceId}")
    public ResponseEntity<Response<WorkspaceResponse>> update(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId,
        @RequestBody @Valid WorkspaceUpdateRequest request
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.update(jwt.getSubject(), workspaceId, request)));
    }

    @Operation(
        summary = "워크스페이스 삭제",
        description = "워크스페이스를 삭제합니다. ADMIN role만 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Response<Void>> delete(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId
    ) {
        workspaceService.delete(jwt.getSubject(), workspaceId);
        return ResponseEntity.ok(Response.ok());
    }

    @Operation(
        summary = "워크스페이스 멤버 조회",
        description = "특정 워크스페이스에 속한 멤버 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "워크스페이스 비소속"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<Response<List<WorkspaceMemberResponse>>> getMembers(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getMembers(jwt.getSubject(), workspaceId)));
    }

    @Operation(
        summary = "멤버 초대",
        description = "특정 사용자를 워크스페이스에 초대합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "초대 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 또는 유저 없음")
    })
    @PostMapping("/{workspaceId}/invitations")
    public ResponseEntity<Response<Void>> invite(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId,
        @RequestBody @Valid InviteRequest request
    ) {
        workspaceService.invite(jwt.getSubject(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }

    @Operation(
        summary = "멤버 초대 수락",
        description = "워크스페이스 초대를 수락하고 멤버로 참여합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "수락 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "초대 정보 없음")
    })
    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<Response<Void>> acceptInvitation(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId
    ) {
        workspaceService.acceptInvitation(jwt.getSubject(), workspaceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }

    @Operation(
        summary = "채널 생성",
        description = "특정 워크스페이스 내부에 새로운 채널을 생성합니다.",
        tags = {"채널"}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "워크스페이스 비소속"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @PostMapping("/{workspaceId}/channels")
    public ResponseEntity<Response<ChannelResponse>> createChannel(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId,
        @RequestBody @Valid ChannelCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response.ok(channelService.create(jwt.getSubject(), workspaceId, request)));
    }

    @Operation(
        summary = "멤버 권한 변경",
        description = "워크스페이스 멤버의 권한(ADMIN/MEMBER)을 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN만 가능)"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 또는 유저 없음")
    })
    @PatchMapping("/{workspaceId}/members")
    public ResponseEntity<Response<Void>> changeRole(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID") @PathVariable String workspaceId,
        @RequestBody @Valid ChangeRoleRequest request
    ) {
        workspaceService.changeRole(jwt.getSubject(), workspaceId, request);
        return ResponseEntity.ok(Response.ok());
    }
}
