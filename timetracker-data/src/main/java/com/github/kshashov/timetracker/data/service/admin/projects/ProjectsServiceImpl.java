package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
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
import java.util.List;
import java.util.Objects;

@Service
public class ProjectsServiceImpl implements ProjectsService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectsRepository projectsRepository;
    private final RolesRepository rolesRepository;
    private final ProjectUsersService projectUsersService;
    private final ProjectActionsService projectActionsService;
    private final ActionsRepository actionsRepository;
    private final ProjectRolesRepository projectRolesRepository;


    @Autowired
    public ProjectsServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            ProjectsRepository projectsRepository,
            RolesRepository rolesRepository,
            ProjectUsersService projectUsersService,
            ProjectActionsService projectActionsService,
            ActionsRepository actionsRepository,
            ProjectRolesRepository projectRolesRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectsRepository = projectsRepository;
        this.rolesRepository = rolesRepository;
        this.projectUsersService = projectUsersService;
        this.projectActionsService = projectActionsService;
        this.actionsRepository = actionsRepository;
        this.projectRolesRepository = projectRolesRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Project updateProject(@NotNull User user, @NotNull Project project) {
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return updateProject(project);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProject(@NotNull User user, Long projectId) {
        if (!rolePermissionsHelper.hasProjectPermission(user, projectId, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return deleteOrDeactivateProject(projectId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Project createProject(@NotNull User user, @NotNull Project project) {
        try {
            project.setIsActive(true);
            return doCreateProject(user, project);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("projects_unique_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Project " + project.getTitle() + " already exists", ex);
                } else {
                    throw new IncorrectArgumentException("Invalid request", ex);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Project updateProject(@NotNull Project project) {
        try {
            return doUpdateProject(project);
        } catch (javax.validation.ConstraintViolationException ex) {
            throw new IncorrectArgumentException("Invalid request", ex);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException casted = (ConstraintViolationException) ex.getCause();
                if ("projects_unique_title".equals(casted.getConstraintName())) {
                    throw new IncorrectArgumentException("Project " + project.getTitle() + " already exists", ex);
                } else {
                    throw new IncorrectArgumentException("Invalid request", ex);
                }
            }
            throw ex;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProject(@NotNull Long projectId) {
        return doDeleteOrDeactivateProject(projectId);
    }

    private Project doCreateProject(@NotNull User user, @NotNull Project project) {
        preValidate(project);

        // TODO check if user can create projects

        // Validate
        if (project.getId() != null) {
            throw new IllegalArgumentException();
        }

        if ((project.getActions() != null) && !project.getActions().isEmpty()) {
            project.getActions().clear();
        }

        if (projectsRepository.existsByTitle(project.getTitle())) {
            throw new IncorrectArgumentException("Project " + project.getTitle() + " already exists");
        }

        // Create project
        project = projectsRepository.save(project);

        // Add highest project role to user
        ProjectRole projectRole = new ProjectRole();
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRole.setRole(rolesRepository.findOneByCode(ProjectRoleType.ADMIN.getCode())); // TODO get rid of string literal
        projectUsersService.createProjectRole(projectRole);

        return project;
    }

    private Project doUpdateProject(@NotNull Project project) {
        preValidate(project);

        // Validate
        Objects.requireNonNull(project.getId());

        if (projectsRepository.existsByTitleAndIdNot(project.getTitle(), project.getId())) {
            throw new IncorrectArgumentException("Project " + project.getTitle() + " already exists");
        }

        // Update
        project = projectsRepository.save(project);

        return project;
    }

    public boolean doDeleteOrDeactivateProject(@NotNull Long projectId) {

        Project project = projectsRepository.findById(projectId).get();

        // Validate
        if (!project.getIsActive()) {
            throw new IncorrectArgumentException("Project is already inactive");
        }

        // Deactivate project's actions
        List<Action> actions = actionsRepository.findByProjectAndIsActive(project, true);
        for (Action action : actions) {
            projectActionsService.deleteOrDeactivateAction(action.getId());
        }

        //  Check if any actions are left
        if (actionsRepository.existsByProject(project)) {
            // Deactivate project
            project.setIsActive(false);
            projectsRepository.save(project);
            return false;
        }

        // Delete project with roles
        projectRolesRepository.deleteByProject(project);
        projectsRepository.deleteById(project.getId());
        return true;
    }

    private void preValidate(@NotNull Project project) {
        if (Strings.isBlank(project.getTitle())) {
            throw new IncorrectArgumentException("Project title is empty");
        }

        if (!project.getIsActive()) {
            throw new IncorrectArgumentException("Inactive project can't be updated");
        }
    }
}
