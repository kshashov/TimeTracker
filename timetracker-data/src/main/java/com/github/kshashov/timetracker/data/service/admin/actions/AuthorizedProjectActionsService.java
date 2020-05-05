package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface AuthorizedProjectActionsService {
    Action createAction(@NotNull User user, @NotNull Long projectId, @NotNull ActionInfo actionInfo);

    Action updateAction(@NotNull User user, @NotNull Long actionId, @NotNull ActionInfo actionInfo);

    void activateAction(@NotNull User user, @NotNull Long actionId);

    boolean deleteOrDeactivateAction(@NotNull User user, @NotNull Long actionId);
}
