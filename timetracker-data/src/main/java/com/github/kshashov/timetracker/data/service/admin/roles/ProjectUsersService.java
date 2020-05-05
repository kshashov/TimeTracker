package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface ProjectUsersService {
    ProjectRole createProjectRole(@NotNull ProjectRole projectRole);

    ProjectRole createProjectRole(@NotNull User user, @NotNull ProjectRole projectRole);

    ProjectRole updateProjectRole(@NotNull ProjectRole projectRole);

    ProjectRole updateProjectRole(@NotNull User user, @NotNull ProjectRole projectRole);

    boolean deleteOrDeactivateProjectRole(@NotNull ProjectRoleIdentity projectRoleIdentity);

    boolean deleteOrDeactivateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity projectRoleIdentity);
}
