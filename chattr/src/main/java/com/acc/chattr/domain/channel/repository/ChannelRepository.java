package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {
    void save(Channel channel);
    Optional<Channel> findById(String channelId);
    List<Channel> findByWorkspaceId(String workspaceId);
    /** 삭제 여부와 무관하게 워크스페이스의 모든 채널 ID 조회 (삭제 시 멤버 정리 용도) */
    List<String> findAllIdsByWorkspaceId(String workspaceId);
}
