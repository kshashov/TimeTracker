package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface ProjectActionsService {
    Action createAction(@NotNull Action action);

    Action createAction(@NotNull User user, @NotNull Action action);

    Action updateAction(@NotNull Action action);

    Action updateAction(@NotNull User user, @NotNull Action action);

    boolean deleteOrDeactivateAction(@NotNull Long actionId);

    boolean deleteOrDeactivateAction(@NotNull User user, Long actionId);
}
