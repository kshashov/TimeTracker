package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface AuthorizedProjectUsersService {
    ProjectRole createProjectRole(@NotNull User user, @NotNull Long projectId, @NotNull Long userId, @NotNull ProjectRoleInfo projectRole);

    ProjectRole updateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity identity, @NotNull ProjectRoleInfo projectRole);

    boolean deleteOrDeactivateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity projectRoleIdentity);
}
