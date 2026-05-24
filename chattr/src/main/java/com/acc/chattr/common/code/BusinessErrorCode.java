package com.acc.chattr.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements Code {

    // ==================== 유저 ====================
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),

    // ==================== 워크스페이스 ====================
    WORKSPACE_NOT_FOUND(404, "존재하지 않는 워크스페이스입니다."),
    WORKSPACE_MEMBER_NOT_FOUND(404, "워크스페이스 멤버가 아닙니다."),
    WORKSPACE_MEMBER_ALREADY_EXISTS(409, "이미 워크스페이스에 가입된 사용자입니다."),
    NOT_WORKSPACE_ADMIN(403, "워크스페이스 관리자만 가능한 작업입니다."),
    LAST_WORKSPACE_ADMIN(409, "워크스페이스에 최소 한 명의 관리자가 있어야 합니다."),
    WORKSPACE_INVITATION_NOT_FOUND(404, "초대 내역이 없습니다."),

    // ==================== 채널 ====================
    CHANNEL_NOT_FOUND(404, "존재하지 않는 채널입니다."),
    CHANNEL_MEMBER_NOT_FOUND(404, "채널 멤버가 아닙니다."),
    CHANNEL_MEMBER_ALREADY_EXISTS(409, "이미 채널에 참여한 사용자입니다."),
    NOT_CHANNEL_MANAGER(403, "채널 관리자(생성자 또는 워크스페이스 관리자)만 가능한 작업입니다."),

    // ==================== DM ====================
    DM_NOT_FOUND(404, "존재하지 않는 DM입니다."),
    DM_ALREADY_EXISTS(409, "이미 DM이 존재합니다."),
    DM_NOT_PARTICIPANT(403, "DM 참여자가 아닙니다."),

    // ==================== 메시지 ====================
    MESSAGE_NOT_FOUND(404, "존재하지 않는 메시지입니다."),
    MESSAGE_NOT_SENDER(403, "본인이 보낸 메시지만 수정/삭제할 수 있습니다.");

    private final int statusCode;
    private final String message;
}
