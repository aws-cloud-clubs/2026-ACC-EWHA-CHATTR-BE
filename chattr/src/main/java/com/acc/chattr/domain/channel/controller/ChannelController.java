package com.acc.chattr.domain.channel.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.channel.dto.AddMemberRequest;
import com.acc.chattr.domain.channel.dto.ChannelMemberResponse;
import com.acc.chattr.domain.channel.dto.ChannelResponse;
import com.acc.chattr.domain.channel.dto.ChannelUpdateRequest;
import com.acc.chattr.domain.channel.service.ChannelService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public ResponseEntity<Response<List<ChannelResponse>>> getChannels(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam String workspaceId
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getChannels(jwt.getSubject(), workspaceId)));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<Response<ChannelResponse>> getChannel(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String channelId
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getChannel(jwt.getSubject(), channelId)));
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<Response<ChannelResponse>> update(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String channelId,
        @RequestBody ChannelUpdateRequest request
    ) {
        return ResponseEntity.ok(Response.ok(channelService.update(jwt.getSubject(), channelId, request)));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Response<Void>> delete(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String channelId
    ) {
        channelService.delete(jwt.getSubject(), channelId);
        return ResponseEntity.ok(Response.ok());
    }

    @GetMapping("/{channelId}/members")
    public ResponseEntity<Response<List<ChannelMemberResponse>>> getMembers(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String channelId
    ) {
        return ResponseEntity.ok(Response.ok(channelService.getMembers(jwt.getSubject(), channelId)));
    }

    @PostMapping("/{channelId}/members")
    public ResponseEntity<Response<Void>> addMember(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String channelId,
        @RequestBody AddMemberRequest request
    ) {
        channelService.addMember(jwt.getSubject(), channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.ok());
    }
}
