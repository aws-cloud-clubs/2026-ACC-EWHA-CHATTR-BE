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

    // user_a_id < user_b_id 순서로 정규화 — (A,B)와 (B,A)가 항상 같은 레코드를 가리킴
    // DynamoDB에서 (user_a_id, user_b_id) GSI 조회 시 이 순서를 전제로 함
    public static Dm create(String id, User user1, User user2) {
        if (user1.getId().compareTo(user2.getId()) <= 0) {
            return new Dm(id, user1, user2);
        }
        return new Dm(id, user2, user1);
    }

    public boolean hasParticipant(User user) {
        return userA.equals(user) || userB.equals(user);
    }
}
