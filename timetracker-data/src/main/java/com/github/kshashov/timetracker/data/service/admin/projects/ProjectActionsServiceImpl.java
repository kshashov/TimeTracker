package com.github.kshashov.timetracker.data.service.admin.projects;

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
import java.util.Objects;

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
    public Action createAction(@NotNull User user, @NotNull Action action) {
        Objects.requireNonNull(action.getProject());

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return createAction(action);
    }

    @Override
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
    public Action updateAction(@NotNull User user, @NotNull Action action) {
        Objects.requireNonNull(action.getProject());

        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return updateAction(action);
    }

    @Override
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
    public boolean deleteOrDeactivateAction(@NotNull User user, Long actionId) {
        Action action = actionsRepository.findOneById(actionId);
        if (!rolePermissionsHelper.hasProjectPermission(user, action.getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return doDeleteOrDeactivateAction(action);
    }

    @Override
    public boolean deleteOrDeactivateAction(@NotNull Long actionId) {
        Action action = actionsRepository.findOneById(actionId);

        return doDeleteOrDeactivateAction(action);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
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

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    private Action doUpdateAction(@NotNull Action action) {
        preValidate(action);

        // Validate
        Objects.requireNonNull(action.getId());

        if (actionsRepository.existsOtherByProjectAndTitle(action.getProject(), action.getTitle(), action)) {
            throw new IncorrectArgumentException("Project action already exists");
        }

        // Create action
        action = actionsRepository.save(action);

        return action;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
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
        Objects.requireNonNull(action.getProject());

        if (Strings.isBlank(action.getTitle())) {
            throw new IncorrectArgumentException("Action title is empty");
        }

        if (!action.getProject().getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }

        if (!action.getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }
    }
}
