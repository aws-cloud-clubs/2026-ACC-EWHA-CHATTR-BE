package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {
    void save(Channel channel);
    Optional<Channel> findById(String channelId);
    List<Channel> findByWorkspaceId(String workspaceId);
}
