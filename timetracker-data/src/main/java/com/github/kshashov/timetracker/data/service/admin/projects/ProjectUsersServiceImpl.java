package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Service
public class ProjectUsersServiceImpl implements ProjectUsersService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectRolesRepository projectRolesRepository;

    @Autowired
    public ProjectUsersServiceImpl(RolePermissionsHelper rolePermissionsHelper, ProjectRolesRepository projectRolesRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectRolesRepository = projectRolesRepository;
    }

    @Override
    public ProjectRole createProjectRole(@NotNull User user, @NotNull ProjectRole projectRole) {
        Objects.requireNonNull(projectRole.getProject());
        Objects.requireNonNull(projectRole.getUser());

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return createProjectRole(projectRole);
    }

    @Override
    public ProjectRole createProjectRole(@NotNull ProjectRole projectRole) {
        return doCreateProjectRole(projectRole);
    }

    @Override
    public ProjectRole updateProjectRole(@NotNull User user, @NotNull ProjectRole projectRole) {
        Objects.requireNonNull(projectRole.getProject());

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return doUpdateProjectRole(projectRole);
    }

    @Override
    public ProjectRole updateProjectRole(@NotNull ProjectRole projectRole) {
        return doUpdateProjectRole(projectRole);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    private ProjectRole doCreateProjectRole(@NotNull ProjectRole projectRole) {
        preValidate(projectRole);

        // Validate
        if (projectRole.getPermissionIdentity() != null) {
            throw new IllegalArgumentException();
        }

        if (projectRolesRepository.hasProjectRole(projectRole.getUser().getId(), projectRole.getProject().getId())) {
            throw new IncorrectArgumentException("Project user already exists");
        }

        // Create
        var id = new ProjectRoleIdentity();
        id.setUserId(projectRole.getUser().getId());
        id.setProjectId(projectRole.getProject().getId());
        projectRole.setPermissionIdentity(id);
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole doUpdateProjectRole(@NotNull ProjectRole projectRole) {
        preValidate(projectRole);

        // Validate
        Objects.requireNonNull(projectRole.getPermissionIdentity());

        // Update
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    private void preValidate(@NotNull ProjectRole projectRole) {
        Objects.requireNonNull(projectRole.getUser());
        Objects.requireNonNull(projectRole.getProject());
        Objects.requireNonNull(projectRole.getRole());
    }
}
