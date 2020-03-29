package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
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
import java.util.Objects;

@Service
public class ProjectsServiceImpl implements ProjectsService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectsRepository projectsRepository;
    private final RolesRepository rolesRepository;
    private final ProjectUsersService projectUsersService;

    @Autowired
    public ProjectsServiceImpl(RolePermissionsHelper rolePermissionsHelper, ProjectsRepository projectsRepository, RolesRepository rolesRepository, ProjectUsersService projectUsersService) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectsRepository = projectsRepository;
        this.rolesRepository = rolesRepository;
        this.projectUsersService = projectUsersService;
    }

    @Override
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
    public Project updateProject(@NotNull User user, @NotNull Project project) {
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return updateProject(project);
    }

    @Override
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


    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
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

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    private Project doUpdateProject(@NotNull Project project) {
        preValidate(project);

        // Validate
        Objects.requireNonNull(project.getId());

        if (projectsRepository.existsOtherByTitle(project.getTitle(), project.getId())) {
            throw new IncorrectArgumentException("Project " + project.getTitle() + " already exists");
        }

        // Update
        project = projectsRepository.save(project);

        return project;
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
