package com.github.kshashov.timetracker.data.entity.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Embeddable
public class ProjectPermissionIdentity implements Serializable {

    @NotNull
    @Column(name = "project_id")
    private Long projectId;

    @NotNull
    @Column(name = "user_id")
    private Long userId;
}
