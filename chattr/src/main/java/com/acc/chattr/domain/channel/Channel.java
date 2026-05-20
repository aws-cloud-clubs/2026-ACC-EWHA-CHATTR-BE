package com.acc.chattr.domain.channel;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.domain.user.User;
import com.acc.chattr.domain.workspace.Workspace;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "channel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {

    @Id
    @Column(name = "channel_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "name", length = 80)
    private String name;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "topic", length = 20)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChannelMember> members = new ArrayList<>();

    private Channel(String id, Workspace workspace, String name, String description, String topic, User createdBy) {
        this.id = id;
        this.workspace = workspace;
        this.name = name;
        this.description = description;
        this.topic = topic;
        this.createdBy = createdBy;
        addMember(createdBy);
    }

    public static Channel create(String id, Workspace workspace, String name, String description, String topic, User createdBy) {
        return new Channel(id, workspace, name, description, topic, createdBy);
    }

    public ChannelMember addMember(User user) {
        ChannelMember member = ChannelMember.create(this, user);
        this.members.add(member);
        return member;
    }

    public void updateInfo(String name, String description, String topic) {
        this.name = name;
        this.description = description;
        this.topic = topic;
    }

    public List<ChannelMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
