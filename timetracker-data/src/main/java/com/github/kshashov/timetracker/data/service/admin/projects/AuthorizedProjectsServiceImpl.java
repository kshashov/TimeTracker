package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.service.admin.roles.ProjectUsersService;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Service
public class AuthorizedProjectsServiceImpl implements AuthorizedProjectsService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectsRepository projectsRepository;
    private final ProjectsService projectsService;
    private final RolesRepository rolesRepository;
    private final ProjectUsersService projectUsersService;

    @Autowired
    public AuthorizedProjectsServiceImpl(
            RolePermissionsHelper rolePermissionsHelper,
            ProjectsRepository projectsRepository,
            ProjectsService projectsService,
            RolesRepository rolesRepository,
            ProjectUsersService projectUsersService) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectsRepository = projectsRepository;
        this.projectsService = projectsService;
        this.rolesRepository = rolesRepository;
        this.projectUsersService = projectUsersService;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Project createProject(@NotNull User user, @NotNull ProjectInfo projectInfo) {
        Project project = projectsService.createProject(projectInfo);

        // Add admin project role to user
        ProjectRole projectRole = new ProjectRole();
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRole.setRole(rolesRepository.findOneByCode(ProjectRoleType.ADMIN.getCode()));
        projectUsersService.createProjectRole(projectRole);

        return project;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Project updateProject(@NotNull User user, @NotNull Long projectId, @NotNull ProjectInfo projectInfo) {
        Project project = projectsRepository.getOne(projectId);
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return projectsService.updateProject(projectId, projectInfo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void activateProject(@NotNull User user, Long projectId) {
        Project project = projectsRepository.getOne(projectId);
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        projectsService.activateProject(projectId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public boolean deleteOrDeactivateProject(@NotNull User user, @NotNull Long projectId) {
        Project project = projectsRepository.findById(projectId).get();
        if (!rolePermissionsHelper.hasProjectPermission(user, project, ProjectPermissionType.EDIT_PROJECT_INFO)) {
            throw new NoPermissionException("You have no permissions to update this project");
        }

        return projectsService.deleteOrDeactivateProject(projectId);
    }
}
