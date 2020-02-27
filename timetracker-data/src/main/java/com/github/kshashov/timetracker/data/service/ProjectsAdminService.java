package com.github.kshashov.timetracker.data.service;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.utils.OptionalResult;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProjectsAdminService {
    private final RolePermissionsHelper rolePermissionsHelper;
    private final ProjectsRepository projectsRepository;
    private final ProjectRolesRepository projectRolesRepository;
    private final RolesRepository rolesRepository;
    private final ActionsRepository actionsRepository;

    @Autowired
    public ProjectsAdminService(RolePermissionsHelper rolePermissionsHelper, ProjectsRepository projectsRepository, ProjectRolesRepository projectRolesRepository, RolesRepository rolesRepository, ActionsRepository actionsRepository) {
        this.rolePermissionsHelper = rolePermissionsHelper;
        this.projectsRepository = projectsRepository;
        this.projectRolesRepository = projectRolesRepository;
        this.rolesRepository = rolesRepository;
        this.actionsRepository = actionsRepository;
    }

    @Transactional
    public OptionalResult<Project> createProject(User user, Project project) {
        // TODO check if user can create projects

        // Validate project
        if (Strings.isBlank(project.getTitle())) {
            return OptionalResult.Fail("Project title is empty");
        }

        if (projectsRepository.hasProject(project.getTitle())) {
            return OptionalResult.Fail("Project already exists");
        }

        // Create project
        project.setIsActive(true);
        project = projectsRepository.save(project);

        // Add highest project role to user
        ProjectRole projectRole = new ProjectRole();
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRole.setRole(rolesRepository.findOneByTitle("project_admin")); // TODO get rid of string literal

        var id = new ProjectRoleIdentity();
        id.setUserId(projectRole.getUser().getId());
        id.setProjectId(projectRole.getProject().getId());
        projectRole.setPermissionIdentity(id);
        projectRolesRepository.save(projectRole);

        return new OptionalResult<>(project, "Project " + project.getTitle() + " has been created");
    }

    public OptionalResult<Project> updateProject(User user, Project project) {
        ProjectRole projectRole = projectRolesRepository.findOneByUserAndProject(user, project);
        if (!rolePermissionsHelper.hasPermission(projectRole.getRole(), ProjectPermissionType.EDIT_PROJECT_INFO)) {
            return OptionalResult.Fail("You have no permissions to update this project");
        }

        if (project.getId() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        // Validate project
        if (Strings.isBlank(project.getTitle())) {
            return OptionalResult.Fail("Project title is empty");
        }

        if (!project.getIsActive()) {
            return OptionalResult.Fail("Inactive project can't be updated");
        }

        // Update
        project = projectsRepository.save(project);

        return new OptionalResult<>(project, "Project " + project.getTitle() + " has been updated");
    }

    public OptionalResult<ProjectRole> createProjectRole(User user, ProjectRole projectRole) {
        if (projectRole.getUser() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (projectRole.getProject() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (projectRole.getRole() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        ProjectRole userProjectRole = projectRolesRepository.findOneByUserAndProject(user, projectRole.getProject());
        if (!rolePermissionsHelper.hasPermission(userProjectRole.getRole(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            return OptionalResult.Fail("You have no permissions to update this project");
        }

        // Validate
        if (user.getId().equals(projectRole.getUser().getId())) {
            return OptionalResult.Fail("Project user cannot be added by the same user");
        }

        if (projectRolesRepository.hasProjectRole(projectRole.getUser().getId(), projectRole.getProject().getId())) {
            return OptionalResult.Fail("Project user already exists");
        }

        // Create
        var id = new ProjectRoleIdentity();
        id.setUserId(projectRole.getUser().getId());
        id.setProjectId(projectRole.getProject().getId());
        projectRole.setPermissionIdentity(id);
        projectRole = projectRolesRepository.save(projectRole);

        return OptionalResult.Success(projectRole);
    }

    public OptionalResult<ProjectRole> updateProjectRole(User user, ProjectRole projectRole) {
        if (projectRole.getPermissionIdentity() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (projectRole.getUser() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (projectRole.getProject() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (projectRole.getRole() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        ProjectRole userProjectRole = projectRolesRepository.findOneByUserAndProject(user, projectRole.getProject());
        if (!rolePermissionsHelper.hasPermission(userProjectRole.getRole(), ProjectPermissionType.EDIT_PROJECT_USERS)) {
            return OptionalResult.Fail("You have no permissions to update this project");
        }

        if (user.getId().equals(projectRole.getUser().getId())) {
            return OptionalResult.Fail("Project user cannot be updated by the same user");
        }

        projectRole = projectRolesRepository.save(projectRole);

        return OptionalResult.Success(projectRole);
    }

    public OptionalResult<Action> createAction(User user, Action action) {
        ProjectRole projectRole = projectRolesRepository.findOneByUserAndProject(user, action.getProject());
        if (!rolePermissionsHelper.hasPermission(projectRole.getRole(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            return OptionalResult.Fail("You have no permissions to update this project");
        }

        if (action.getId() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        if (action.getProject() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        // Validate action
        if (Strings.isBlank(action.getTitle())) {
            return OptionalResult.Fail("Action title is empty");
        }

        if (!action.getProject().getIsActive()) {
            return OptionalResult.Fail("Inactive project can't be updated");
        }

        if (actionsRepository.hasAction(action.getProject().getId(), action.getTitle())) {
            return OptionalResult.Fail("Project action already exists");
        }

        // Create action
        action.setIsActive(true);
        action = actionsRepository.save(action);

        return OptionalResult.Success(action);
    }

    public OptionalResult<Action> updateAction(User user, Action action) {
        if (action.getProject() == null) {
            return OptionalResult.Fail("Internal server error");
        }

        ProjectRole projectRole = projectRolesRepository.findOneByUserAndProject(user, action.getProject());
        if (!rolePermissionsHelper.hasPermission(projectRole.getRole(), ProjectPermissionType.EDIT_PROJECT_ACTIONS)) {
            return OptionalResult.Fail("You have no permissions to update this project");
        }

        // Validate action
        if (Strings.isBlank(action.getTitle())) {
            return OptionalResult.Fail("Action title is empty");
        }

        if (!action.getProject().getIsActive()) {
            return OptionalResult.Fail("Inactive project can't be updated");
        }

        if (!action.getIsActive()) {
            return OptionalResult.Fail("Inactive action can't be updated");
        }

        // Create action
        action = actionsRepository.save(action);

        return OptionalResult.Success(action);
    }

/*    public List<projectRole> findProjectsWithRoles(User user) {
        // TODO find available for user
    }

    public List<projectRole> findUsersWithRoles(Project user) {
        // TODO users with roles
    }*/

}
