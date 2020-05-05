package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class ProjectActionsServiceImpl implements ProjectActionsService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ActionsRepository actionsRepository;
    private final EntriesRepository entiesRepository;

    @Autowired
    public ProjectActionsServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            ActionsRepository actionsRepository,
            EntriesRepository entiesRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.actionsRepository = actionsRepository;
        this.entiesRepository = entiesRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action createAction(@NotNull User user, @NotNull Action action) {
        if ((action.getProject() == null)
                || !rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to create this project");
        }

        return createAction(action);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action updateAction(@NotNull User user, @NotNull Action action) {
        if ((action.getProject() == null)
                || !rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return updateAction(action);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void activateAction(@NotNull User user, Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        doActivateAction(action);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateAction(@NotNull User user, Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return doDeleteOrDeactivateAction(action);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action createAction(@NotNull Action action) {
        try {
            action.setIsActive(true);
            return doCreateAction(action);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("actions_unique_project_id_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Action " + action.getTitle() + " already exists", ex);
                } else {
                    throw new IncorrectArgumentException("Invalid request", ex);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action updateAction(@NotNull Action action) {
        try {
            return doUpdateAction(action);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("actions_unique_project_id_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Action " + action.getTitle() + " already exists", ex);
                } else {
                    throw new IncorrectArgumentException("Invalid request", ex);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void activateAction(@NotNull Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        doActivateAction(action);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateAction(@NotNull Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

        return doDeleteOrDeactivateAction(action);
    }

    private Action doCreateAction(@NotNull Action action) {
        preValidate(action);

        // Validate
        if (action.getId() != null) {
            throw new IllegalArgumentException();
        }

        if (actionsRepository.existsByProjectAndTitle(action.getProject(), action.getTitle())) {
            throw new IncorrectArgumentException("Project action already exists");
        }

        // Create action
        action = actionsRepository.save(action);

        return action;
    }

    private Action doUpdateAction(@NotNull Action action) {
        preValidate(action);

        // Validate
        if (action.getId() == null) {
            throw new IllegalArgumentException();
        }

        if (actionsRepository.existsByProjectAndTitleAndIdNot(action.getProject(), action.getTitle(), action.getId())) {
            throw new IncorrectArgumentException("Project action already exists");
        }

        // Create action
        action = actionsRepository.save(action);

        return action;
    }

    private void doActivateAction(@NotNull Action action) {

        // Validate
        if (action.getIsActive()) {
            throw new IncorrectArgumentException("Action is already active");
        }

        if (!action.getProject().getIsActive()) {
            throw new IncorrectArgumentException("Project is inactive");
        }

        action.setIsActive(true);
        actionsRepository.save(action);
    }

    private boolean doDeleteOrDeactivateAction(@NotNull Action action) {

        if (!action.getIsActive()) {
            throw new IncorrectArgumentException("Action is already inactive");
        }

        // Delete open entries with action
        entiesRepository.deleteByActionAndIsClosed(action, false);

        //  Check if any entries are left
        if (entiesRepository.existsByAction(action)) {
            // Deactivate action
            action.setIsActive(false);
            actionsRepository.save(action);
            return false;
        }

        actionsRepository.deleteById(action.getId());
        return true;
    }

    private void preValidate(@NotNull Action action) {
        if (action.getProject() == null) {
            throw new IncorrectArgumentException("Action project is empty");
        }

        if (Strings.isBlank(action.getTitle())) {
            throw new IncorrectArgumentException("Action title is empty");
        }

        if (!action.getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }
    }
}
