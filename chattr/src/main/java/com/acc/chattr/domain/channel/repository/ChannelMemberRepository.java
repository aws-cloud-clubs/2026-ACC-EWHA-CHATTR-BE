package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.ChannelMember;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository {
    void save(ChannelMember member);
    Optional<ChannelMember> findByChannelIdAndUserId(String channelId, String userId);
    List<ChannelMember> findByChannelId(String channelId);
    List<ChannelMember> findByUserId(String userId);
}
