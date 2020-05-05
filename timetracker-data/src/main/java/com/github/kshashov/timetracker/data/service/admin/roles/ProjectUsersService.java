package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;

import javax.validation.constraints.NotNull;

public interface ProjectUsersService {
    ProjectRole createProjectRole(@NotNull Long projectId, @NotNull Long userId, @NotNull ProjectRoleInfo projectRole);

    ProjectRole updateProjectRole(@NotNull ProjectRoleIdentity identity, @NotNull ProjectRoleInfo projectRole);

    boolean deleteOrDeactivateProjectRole(@NotNull ProjectRoleIdentity projectRoleIdentity);
}
