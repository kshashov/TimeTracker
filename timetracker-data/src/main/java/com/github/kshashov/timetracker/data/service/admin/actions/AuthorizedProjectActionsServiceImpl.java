package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class AuthorizedProjectActionsServiceImpl implements AuthorizedProjectActionsService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ActionsRepository actionsRepository;
    private final ProjectActionsService projectActionsService;
    private final ProjectsRepository projectsRepository;

    @Autowired
    public AuthorizedProjectActionsServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            ActionsRepository actionsRepository,
            ProjectActionsService projectActionsService,
            ProjectsRepository projectsRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.actionsRepository = actionsRepository;
        this.projectActionsService = projectActionsService;
        this.projectsRepository = projectsRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action createAction(@NotNull User user, @NotNull Long projectId, @NotNull ActionInfo actionInfo) {
        Project project = projectsRepository.getOne(projectId);
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to create this project");
        }

        return projectActionsService.createAction(projectId, actionInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action updateAction(@NotNull User user, @NotNull Long actionId, @NotNull ActionInfo actionInfo) {
        Action action = actionsRepository.findWithProjectById(actionId);
        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return projectActionsService.updateAction(actionId, actionInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void activateAction(@NotNull User user, Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        projectActionsService.activateAction(actionId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateAction(@NotNull User user, Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return projectActionsService.deleteOrDeactivateAction(actionId);
    }
}
