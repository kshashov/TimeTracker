package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface AuthorizedProjectsService {
    Project createProject(@NotNull User user, @NotNull ProjectInfo project);

    Project updateProject(@NotNull User user, @NotNull Long projectId, @NotNull ProjectInfo project);

    void activateProject(@NotNull User user, @NotNull Long projectId);

    boolean deleteOrDeactivateProject(@NotNull User user, @NotNull Long projectId);
}
