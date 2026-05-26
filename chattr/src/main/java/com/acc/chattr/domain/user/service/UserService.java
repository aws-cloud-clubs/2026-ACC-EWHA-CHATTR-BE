package com.acc.chattr.domain.user.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.common.response.CursorPageResponse;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
import com.acc.chattr.domain.workspace.repository.WorkspaceMemberRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public UserService(UserRepository userRepository,
                       WorkspaceRepository workspaceRepository,
                       WorkspaceMemberRepository workspaceMemberRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public UserResponse getMe(String cognitoSub) {
        User user = userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public CursorPageResponse<UserResponse> getAllUsers(int size, String cursor) {
        return mapResponse(userRepository.findAll(size, cursor));
    }

    public CursorPageResponse<UserResponse> searchUsers(String query, int size, String cursor) {
        return mapResponse(userRepository.findByQuery(query, size, cursor));
    }

    public CursorPageResponse<UserResponse> getOnlineUsers(int size, String cursor) {
        return mapResponse(userRepository.findOnlineUsers(size, cursor));
    }

    public CursorPageResponse<UserResponse> getWorkspaceUsers(String cognitoSub, String workspaceId,
                                                               String query, int size, String cursor) {
        User requester = userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_NOT_FOUND));
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, requester.getId())
            .filter(m -> !m.isPending())
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND));

        List<String> userIds = workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
            .map(WorkspaceMember::getUserId)
            .toList();
        // 워크스페이스 멤버는 수가 제한적이므로 BatchGet 후 메모리 필터링
        List<UserResponse> filtered = userRepository.findAllByIds(userIds).stream()
            .filter(u -> query == null || query.isBlank()
                || u.getEmail().contains(query)
                || u.getNickname().contains(query))
            .map(UserResponse::from)
            .toList();

        // 워크스페이스 멤버 수는 제한적이므로 메모리 슬라이싱 허용
        int from = 0;
        // cursor가 있으면 offset으로 디코딩 (워크스페이스 멤버용 단순 정수 커서)
        if (cursor != null && !cursor.isBlank()) {
            try {
                from = Integer.parseInt(new String(java.util.Base64.getUrlDecoder().decode(cursor)));
            } catch (Exception ignored) {}
        }
        int to = Math.min(from + size, filtered.size());
        List<UserResponse> content = from >= filtered.size() ? List.of() : filtered.subList(from, to);
        String nextCursor = to < filtered.size()
            ? java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.valueOf(to).getBytes())
            : null;
        return CursorPageResponse.of(content, nextCursor);
    }

    private CursorPageResponse<UserResponse> mapResponse(CursorPageResponse<User> page) {
        List<UserResponse> content = page.content().stream().map(UserResponse::from).toList();
        return CursorPageResponse.of(content, page.nextCursor());
    }
}
