package com.acc.chattr.domain.channel;

import com.acc.chattr.domain.user.User;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channel_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelMember {

    @EmbeddedId
    private ChannelMemberId id;

    @MapsId("channelId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ChannelMember(Channel channel, User user) {
        this.id = new ChannelMemberId(channel.getId(), user.getId());
        this.channel = channel;
        this.user = user;
    }

    public static ChannelMember create(Channel channel, User user) {
        return new ChannelMember(channel, user);
    }
}
