package com.acc.chattr.domain.channel.controller;

import com.acc.chattr.common.response.CursorPageResponse;
import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.channel.dto.AddMemberRequest;
import com.acc.chattr.domain.channel.dto.ChannelMemberResponse;
import com.acc.chattr.domain.channel.dto.ChannelResponse;
import com.acc.chattr.domain.channel.dto.ChannelUpdateRequest;
import com.acc.chattr.domain.channel.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "채널", description = "채널 생성·조회·수정·삭제 및 멤버 관리 API")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Operation(
        summary = "채널 목록 조회",
        description = "특정 워크스페이스에 속한 채널 목록을 조회합니다. page/size 기반 pagination을 사용합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "워크스페이스 비소속"),
        @ApiResponse(responseCode = "404", description = "워크스페이스 없음")
    })
    @GetMapping
    public ResponseEntity<Response<CursorPageResponse<ChannelResponse>>> getChannels(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "워크스페이스 ID", required = true) @RequestParam String workspaceId,
        @Parameter(description = "페이지 크기") @Min(1) @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "다음 페이지 커서 (첫 페이지는 생략)") @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getChannels(jwt.getSubject(), workspaceId, size, cursor)));
    }

    @Operation(
        summary = "채널 단건 조회",
        description = "특정 채널의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @GetMapping("/{channelId}")
    public ResponseEntity<Response<ChannelResponse>> getChannel(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "채널 ID") @PathVariable String channelId
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getChannel(jwt.getSubject(), channelId)));
    }

    @Operation(
        summary = "채널 수정",
        description = "채널 이름, 설명, 토픽 등의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (채널 관리자만 가능)"),
        @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @PatchMapping("/{channelId}")
    public ResponseEntity<Response<ChannelResponse>> update(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "채널 ID") @PathVariable String channelId,
        @RequestBody @Valid ChannelUpdateRequest request
    ) {
        return ResponseEntity.ok(Response.ok(channelService.update(jwt.getSubject(), channelId, request)));
    }

    @Operation(
        summary = "채널 삭제",
        description = "채널을 삭제합니다. 채널 관리자만 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (채널 관리자만 가능)"),
        @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Response<Void>> delete(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "채널 ID") @PathVariable String channelId
    ) {
        channelService.delete(jwt.getSubject(), channelId);
        return ResponseEntity.ok(Response.ok());
    }

    @Operation(
        summary = "채널 멤버 조회",
        description = "특정 채널에 참여 중인 멤버 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "채널 없음")
    })
    @GetMapping("/{channelId}/members")
    public ResponseEntity<Response<List<ChannelMemberResponse>>> getMembers(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "채널 ID") @PathVariable String channelId
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getMembers(jwt.getSubject(), channelId)));
    }

    @Operation(
        summary = "채널 멤버 추가",
        description = "특정 사용자를 채널에 초대하거나 추가합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "추가 성공"),
        @ApiResponse(responseCode = "400", description = "요청 값 오류"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "채널 또는 유저 없음")
    })
    @PostMapping("/{channelId}/members")
    public ResponseEntity<Response<Void>> addMember(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "채널 ID") @PathVariable String channelId,
        @RequestBody @Valid AddMemberRequest request
    ) {
        channelService.addMember(jwt.getSubject(), channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }
}
