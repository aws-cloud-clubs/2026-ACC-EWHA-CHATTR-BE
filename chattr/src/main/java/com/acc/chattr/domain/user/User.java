package com.acc.chattr.domain.user;

import com.acc.chattr.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Column(name = "cognito_sub", length = 255, nullable = false, unique = true)
    private String cognitoSub;

    @Column(name = "is_online", nullable = false)
    private boolean online;

    private User(String id, String email, String nickname, String cognitoSub) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.cognitoSub = cognitoSub;
        this.online = false;
    }

    public static User create(String id, String email, String nickname, String cognitoSub) {
        return new User(id, email, nickname, cognitoSub);
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    public void markOnline() {
        this.online = true;
    }

    public void markOffline() {
        this.online = false;
    }
}
