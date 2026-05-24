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

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ChannelService channelService;

    public WorkspaceController(WorkspaceService workspaceService, ChannelService channelService) {
        this.workspaceService = workspaceService;
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<Response<WorkspaceResponse>> create(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid WorkspaceCreateRequest request
    ) {
        WorkspaceResponse response = workspaceService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok(response));
    }

    @GetMapping
    public ResponseEntity<Response<List<WorkspaceResponse>>> getMyWorkspaces(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getMyWorkspaces(jwt.getSubject())));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<Response<WorkspaceResponse>> getWorkspace(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getWorkspace(jwt.getSubject(), workspaceId)));
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<Response<WorkspaceResponse>> update(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId,
        @RequestBody @Valid WorkspaceUpdateRequest request
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.update(jwt.getSubject(), workspaceId, request)));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Response<Void>> delete(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId
    ) {
        workspaceService.delete(jwt.getSubject(), workspaceId);
        return ResponseEntity.ok(Response.ok());
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<Response<List<WorkspaceMemberResponse>>> getMembers(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId
    ) {
        return ResponseEntity.ok(Response.ok(workspaceService.getMembers(jwt.getSubject(), workspaceId)));
    }

    @PostMapping("/{workspaceId}/invitations")
    public ResponseEntity<Response<Void>> invite(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId,
        @RequestBody @Valid InviteRequest request
    ) {
        workspaceService.invite(jwt.getSubject(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<Response<Void>> acceptInvitation(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId
    ) {
        workspaceService.acceptInvitation(jwt.getSubject(), workspaceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }

    @PostMapping("/{workspaceId}/channels")
    public ResponseEntity<Response<ChannelResponse>> createChannel(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId,
        @RequestBody @Valid ChannelCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response.ok(channelService.create(jwt.getSubject(), workspaceId, request)));
    }

    @PatchMapping("/{workspaceId}/members")
    public ResponseEntity<Response<Void>> changeRole(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String workspaceId,
        @RequestBody @Valid ChangeRoleRequest request
    ) {
        workspaceService.changeRole(jwt.getSubject(), workspaceId, request);
        return ResponseEntity.ok(Response.ok());
    }
}
