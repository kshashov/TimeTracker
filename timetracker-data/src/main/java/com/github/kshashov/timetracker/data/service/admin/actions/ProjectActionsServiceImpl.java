package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
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
    private final ActionsRepository actionsRepository;
    private final EntriesRepository entiesRepository;
    private final ProjectsRepository projectsRepository;

    @Autowired
    public ProjectActionsServiceImpl(
            ActionsRepository actionsRepository,
            EntriesRepository entiesRepository,
            ProjectsRepository projectsRepository) {
        this.actionsRepository = actionsRepository;
        this.entiesRepository = entiesRepository;
        this.projectsRepository = projectsRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action createAction(@NotNull Long projectId, @NotNull ActionInfo actionInfo) {
        try {
            return doCreateAction(projectId, actionInfo);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("actions_unique_project_id_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Action " + actionInfo.getTitle() + " already exists", ex);
                } else {
                    throw new IncorrectArgumentException("Invalid request", ex);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Action updateAction(@NotNull Long actionId, @NotNull ActionInfo actionInfo) {
        try {
            return doUpdateAction(actionId, actionInfo);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("actions_unique_project_id_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Action " + actionInfo.getTitle() + " already exists", ex);
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
        doActivateAction(actionId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateAction(@NotNull Long actionId) {
        return doDeleteOrDeactivateAction(actionId);
    }

    private Action doCreateAction(@NotNull Long projectId, @NotNull ActionInfo actionInfo) {
        preValidate(actionInfo);
        Project project = projectsRepository.getOne(projectId);

        // Validate
        if (actionsRepository.existsByProjectAndTitle(project, actionInfo.getTitle())) {
            throw new IncorrectArgumentException("Project action already exists");
        }

        // Create action
        Action action = new Action();
        action.setTitle(actionInfo.getTitle());
        action.setIsActive(true);
        action.setProject(project);
        action = actionsRepository.save(action);

        return action;
    }

    private Action doUpdateAction(@NotNull Long actionId, @NotNull ActionInfo actionInfo) {
        preValidate(actionInfo);

        Action action = actionsRepository.findWithProjectById(actionId);

        // Validate
        if (actionsRepository.existsByProjectAndTitleAndIdNot(action.getProject(), actionInfo.getTitle(), action.getId())) {
            throw new IncorrectArgumentException("Project action already exists");
        }

        if (!action.getIsActive()) {
            throw new IncorrectArgumentException("Inactive action can't be updated");
        }

        // Create action
        action.setTitle(actionInfo.getTitle());
        action = actionsRepository.save(action);

        return action;
    }

    private void doActivateAction(@NotNull Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

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

    private boolean doDeleteOrDeactivateAction(@NotNull Long actionId) {
        Action action = actionsRepository.findWithProjectById(actionId);

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

    private void preValidate(@NotNull ActionInfo actionInfo) {
        if (Strings.isBlank(actionInfo.getTitle())) {
            throw new IncorrectArgumentException("Action title is empty");
        }
    }
}
