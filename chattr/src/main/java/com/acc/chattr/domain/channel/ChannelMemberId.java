package com.acc.chattr.domain.channel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelMemberId implements Serializable {

    @Column(name = "channel_id", length = 36, nullable = false)
    private String channelId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
}
