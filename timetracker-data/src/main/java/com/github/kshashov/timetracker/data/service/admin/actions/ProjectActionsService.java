package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.data.entity.Action;

import javax.validation.constraints.NotNull;

public interface ProjectActionsService {
    Action createAction(@NotNull Long projectId, @NotNull ActionInfo actionInfo);

    Action updateAction(@NotNull Long actionId, @NotNull ActionInfo actionInfo);

    void activateAction(@NotNull Long actionId);

    boolean deleteOrDeactivateAction(@NotNull Long actionId);

}
