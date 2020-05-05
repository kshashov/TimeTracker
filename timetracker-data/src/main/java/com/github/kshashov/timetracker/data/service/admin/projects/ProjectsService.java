package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.data.entity.Project;

import javax.validation.constraints.NotNull;

public interface ProjectsService {
    Project createProject(@NotNull ProjectInfo projectInfo);

    Project updateProject(@NotNull Long projectId, @NotNull ProjectInfo project);

    void activateProject(@NotNull Long projectId);

    boolean deleteOrDeactivateProject(@NotNull Long projectId);
}
