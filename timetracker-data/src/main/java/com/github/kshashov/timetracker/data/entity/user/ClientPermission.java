package com.github.kshashov.timetracker.data.entity.user;

import com.github.kshashov.timetracker.data.entity.Project;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "client_roles", schema = "public")
public class ClientPermission {

    @EmbeddedId
    private ClientPermissionIdentity permissionIdentity;

    @MapsId("project_id")
    @OneToOne
    private Project project;

    @MapsId("user_id")
    @OneToOne
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
