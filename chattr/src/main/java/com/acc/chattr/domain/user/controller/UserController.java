package com.acc.chattr.domain.user.controller;

import com.acc.chattr.common.response.Response;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<Response<UserResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Response.ok(userService.getMe(jwt.getSubject())));
    }

    @GetMapping
    public ResponseEntity<Response<List<UserResponse>>> getUsers(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) Boolean isOnline
    ) {
        if (Boolean.TRUE.equals(isOnline)) {
            return ResponseEntity.ok(Response.ok(userService.getOnlineUsers()));
        }
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(Response.ok(userService.searchUsers(query)));
        }
        return ResponseEntity.ok(Response.ok(userService.getAllUsers()));
    }
}
