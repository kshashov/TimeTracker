package com.github.kshashov.timetracker.data.entity.user;

import com.github.kshashov.timetracker.data.entity.BaseEntity;
import com.github.kshashov.timetracker.data.entity.Project;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "project_roles", schema = "public")
public class ProjectRoles implements BaseEntity {

    @EmbeddedId
    private ProjectRolesIdentity permissionIdentity;

    @MapsId("project_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @MapsId("user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

}
