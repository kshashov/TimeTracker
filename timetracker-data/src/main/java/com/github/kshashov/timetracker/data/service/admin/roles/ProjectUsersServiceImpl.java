package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class ProjectUsersServiceImpl implements ProjectUsersService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectRolesRepository projectRolesRepository;
    private final EntriesRepository entiesRepository;
    private final RolesRepository rolesRepository;

    @Autowired
    public ProjectUsersServiceImpl(RolePermissionsHelper rolePermissionsHelper, ProjectRolesRepository projectRolesRepository, EntriesRepository entiesRepository, RolesRepository rolesRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectRolesRepository = projectRolesRepository;
        this.entiesRepository = entiesRepository;
        this.rolesRepository = rolesRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole createProjectRole(@NotNull User user, @NotNull ProjectRole projectRole) {
        preValidate(projectRole);

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return doCreateProjectRole(projectRole);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole updateProjectRole(@NotNull User user, @NotNull ProjectRole projectRole) {
        preValidate(projectRole);

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            throw new NoPermissionException("Project user cannot be updated by the same user");
        }

        return doUpdateProjectRole(projectRole);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProjectRole(@NotNull User user, @NotNull ProjectRoleIdentity projectRoleIdentity) {
        ProjectRole projectRole = projectRolesRepository.findFullByIdentity(projectRoleIdentity);

        if (!rolePermissionsHelper.hasProjectPermission(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return doDeleteOrDeactivateProjectRole(projectRole);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole createProjectRole(@NotNull ProjectRole projectRole) {
        preValidate(projectRole);
        return doCreateProjectRole(projectRole);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public ProjectRole updateProjectRole(@NotNull ProjectRole projectRole) {
        preValidate(projectRole);
        return doUpdateProjectRole(projectRole);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProjectRole(@NotNull ProjectRoleIdentity projectRoleIdentity) {
        ProjectRole projectRole = projectRolesRepository.findFullByIdentity(projectRoleIdentity);
        return doDeleteOrDeactivateProjectRole(projectRole);
    }

    private ProjectRole doCreateProjectRole(@NotNull ProjectRole projectRole) {
        // Validate
        if (projectRole.getIdentity() != null) {
            throw new IllegalArgumentException();
        }

        if (projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())) {
            throw new IncorrectArgumentException("Project user already exists");
        }

        // Create
        var id = new ProjectRoleIdentity();
        id.setUserId(projectRole.getUser().getId());
        id.setProjectId(projectRole.getProject().getId());
        projectRole.setIdentity(id);
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    public ProjectRole doUpdateProjectRole(@NotNull ProjectRole projectRole) {
        // Validate
        if (projectRole.getIdentity() == null) {
            throw new IllegalArgumentException();
        }

        // Update
        projectRole = projectRolesRepository.save(projectRole);

        return projectRole;
    }

    private boolean doDeleteOrDeactivateProjectRole(@NotNull ProjectRole projectRole) {

        if (ProjectRoleType.isInactive(projectRole.getRole())) {
            throw new IncorrectArgumentException("Project role is already inactive");
        }

        // Delete open entries with user
        entiesRepository.deleteByUserAndActionProjectAndIsClosed(projectRole.getUser(), projectRole.getProject(), false);

        //  Check if any entries are left
        if (entiesRepository.existsByUserAndActionProject(projectRole.getUser(), projectRole.getProject())) {
            // Deactivate project role
            Role role = rolesRepository.findOneByCode(ProjectRoleType.INACTIVE.getCode());
            projectRole.setRole(role);
            projectRolesRepository.save(projectRole);
            return false;
        }

        projectRolesRepository.deleteById(projectRole.getIdentity());
        return true;
    }

    private void preValidate(@NotNull ProjectRole projectRole) {
        if (projectRole.getUser() == null) {
            throw new IncorrectArgumentException("Role user is empty");
        }

        if (projectRole.getProject() == null) {
            throw new IncorrectArgumentException("Role project user is empty");
        }

        if (projectRole.getRole() == null) {
            throw new IncorrectArgumentException("Project role is empty");
        }
    }
}
