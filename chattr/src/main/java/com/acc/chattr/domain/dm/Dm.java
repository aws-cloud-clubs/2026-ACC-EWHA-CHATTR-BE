package com.acc.chattr.domain.dm;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dm")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dm extends BaseEntity {

    @Id
    @Column(name = "dm_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    private Dm(String id, User userA, User userB) {
        this.id = id;
        this.userA = userA;
        this.userB = userB;
    }

    public static Dm create(String id, User userA, User userB) {
        return new Dm(id, userA, userB);
    }

    public boolean hasParticipant(User user) {
        return userA.equals(user) || userB.equals(user);
    }
}
