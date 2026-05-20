package com.acc.chattr.domain.message;

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
public class MessageId implements Serializable {

    @Column(name = "message_id", length = 36, nullable = false)
    private String messageId;

    @Column(name = "sender_id", length = 36, nullable = false)
    private String senderId;
}
