package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class AuthorizedProjectUsersServiceImpl implements AuthorizedProjectUsersService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectRolesRepository projectRolesRepository;
    private final ProjectUsersService projectUsersService;
    private final ProjectsRepository projectsRepository;

    @Autowired
    public AuthorizedProjectUsersServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            ProjectRolesRepository projectRolesRepository,
            ProjectUsersService projectUsersService,
            ProjectsRepository projectsRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectRolesRepository = projectRolesRepository;
        this.projectUsersService = projectUsersService;
        this.projectsRepository = projectsRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole createProjectRole(@NotNull User user, @NotNull Long projectId, @NotNull Long userId, @NotNull ProjectRoleInfo projectRoleInfo) {
        Project project = projectsRepository.getOne(projectId);

        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(userId)) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return projectUsersService.createProjectRole(projectId, userId, projectRoleInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole updateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity identity, @NotNull ProjectRoleInfo projectRoleInfo) {
        ProjectRole projectRole = projectRolesRepository.getOne(identity);

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(identity.getUserId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return projectUsersService.updateProjectRole(identity, projectRoleInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity projectRoleIdentity) {
        ProjectRole projectRole = projectRolesRepository.findFullByIdentity(projectRoleIdentity);

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return projectUsersService.deleteOrDeactivateProjectRole(projectRoleIdentity);
    }
}
