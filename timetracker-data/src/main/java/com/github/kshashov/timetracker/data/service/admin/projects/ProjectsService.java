package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface ProjectsService {
    Project createProject(@NotNull User user, @NotNull Project project);

    Project updateProject(@NotNull User user, @NotNull Project project);

    Project updateProject(@NotNull Project project);

    void activateProject(@NotNull User user, @NotNull Long projectId);

    void activateProject(@NotNull Long projectId);

    boolean deleteOrDeactivateProject(@NotNull User user, @NotNull Long projectId);

    boolean deleteOrDeactivateProject(@NotNull Long projectId);
}
