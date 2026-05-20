package com.acc.chattr.domain.workspace;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "workspace")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workspace extends BaseEntity {

    @Id
    @Column(name = "workspace_id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMember> members = new ArrayList<>();

    private Workspace(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Workspace create(String id, String name, User owner) {
        Workspace workspace = new Workspace(id, name);
        workspace.addMember(owner, WorkspaceRole.ADMIN);
        return workspace;
    }

    public void rename(String name) {
        this.name = name;
    }

    public WorkspaceMember addMember(User user, WorkspaceRole role) {
        WorkspaceMember member = WorkspaceMember.create(this, user, role);
        this.members.add(member);
        return member;
    }

    public List<WorkspaceMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
