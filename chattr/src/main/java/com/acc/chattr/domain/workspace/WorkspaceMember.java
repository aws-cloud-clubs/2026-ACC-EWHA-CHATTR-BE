package com.acc.chattr.domain.workspace;

import com.acc.chattr.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "workspace_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember {

    @EmbeddedId
    private WorkspaceMemberId id;

    @MapsId("workspaceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10, nullable = false)
    private WorkspaceRole role;

    @Column(name = "field", length = 255)
    private String field;

    private WorkspaceMember(Workspace workspace, User user, WorkspaceRole role) {
        this.id = new WorkspaceMemberId(workspace.getId(), user.getId());
        this.workspace = workspace;
        this.user = user;
        this.role = role;
    }

    public static WorkspaceMember create(Workspace workspace, User user, WorkspaceRole role) {
        return new WorkspaceMember(workspace, user, role);
    }

    public void changeRole(WorkspaceRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == WorkspaceRole.ADMIN;
    }
}
