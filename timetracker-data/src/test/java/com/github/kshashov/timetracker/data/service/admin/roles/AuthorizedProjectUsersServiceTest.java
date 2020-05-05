package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.service.admin.actions.ProjectActionsService;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import static com.github.kshashov.timetracker.data.PermissionsMock.whenPermission;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class AuthorizedProjectUsersServiceTest extends BaseProjectTest {
    @MockBean
    private RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    protected RolesRepository rolesRepository;
    @Autowired
    private ProjectUsersService projectUsersService2;
    @Autowired
    private AuthorizedProjectUsersService projectUsersService;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private ProjectRolesRepository projectRolesRepository;
    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;
    @Autowired
    private ProjectActionsService actionsService;
    @Autowired
    private ProjectsService projectService;

    //
    // CREATE BY USER
    //

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.createRole_CorrectUser.sql")
    void createRole_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("createRole_CorrectUser");
        User user = getUser();
        Project project = getProject();

        // Make user to has EDIT_PROJECT_USERS project permission
        whenPermission(rolePermissionsHelper).allow(user2, project, ProjectPermissionType.EDIT_PROJECT_USERS);

        // Create
        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isFalse();

        ProjectRoleInfo projectRoleInfo = correctRole(getUserRole());
        projectUsersService.createProjectRole(user2, project.getId(), user.getId(), projectRoleInfo);

        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isTrue();
        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(user.getId(), project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRoleInfo.getRoleId());
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.createRole_UserHasNoPermission.sql")
    void createRole_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("createRole_UserHasNoPermission");
        User user = getUser();
        Project project = getProject();

        ProjectRoleInfo projectRoleInfo = correctRole(getUserRole());

        // Make user to has no EDIT_PROJECT_USERS project permission
        whenPermission(rolePermissionsHelper).deny(user2, project, ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(user2, project.getId(), user.getId(), projectRoleInfo))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(user, project.getId(), user.getId(), projectRoleInfo))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // UPDATE BY USER
    //

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.updateRole_CorrectUser.sql")
    void updateRole_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("updateRole_CorrectUser");
        User user = getUser();
        Project project = getProject();

        // Create
        ProjectRole projectRole = projectUsersService2.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));

        // Make user to has EDIT_PROJECT_USERS project permission
        whenPermission(rolePermissionsHelper).allow(user2, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS);

        // Update role
        ProjectRoleInfo projectRoleInfo = correctRole(getAdminRole());
        projectUsersService.updateProjectRole(user2, projectRole.getIdentity(), projectRoleInfo);

        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(user.getId(), project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRole.getRole().getId());
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.updateRole_UserHasNoPermission.sql")
    void updateRole_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("updateRole_UserHasNoPermission");
        User user = getUser();
        Project project = getProject();

        ProjectRole projectRole = projectUsersService2.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));
        ProjectRoleInfo projectRoleInfo = correctRole(getAdminRole());

        // Make user to has no EDIT_PROJECT_USERS project permission
        whenPermission(rolePermissionsHelper).deny(user2, project, ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(user2, projectRole.getIdentity(), projectRoleInfo))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(user, projectRole.getIdentity(), projectRoleInfo))
                .isInstanceOf(NoPermissionException.class);

    }

    //
    // DELETE BY USER
    //

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_CorrectUser.sql")
    void deleteOrDeactivateProjectRole_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();
        User user2 = getUsersRepository().findOneByEmail("deleteOrDeactivateProjectRole_CorrectUser");
        ProjectRole projectRole = projectUsersService2.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);

        // Make user to has EDIT_PROJECT_USERS project permission
        //ArgumentMatcher<Project> argumentMatcher = project -> project.getId().equals(projectRole.getProject().getId());
        whenPermission(rolePermissionsHelper).allow(user2, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(user2, projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_UserHasNoPermission.sql")
    void deleteOrDeactivateProjectRole_UserHasNoPermission_ExceptionThrown() {
        User user2 = getUsersRepository().findOneByEmail("deleteOrDeactivateProjectRole_UserHasNoPermission");
        User user = getUser();
        Project project = getProject();

        ProjectRole projectRole = projectUsersService2.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);

        // Make user to has no EDIT_PROJECT_USERS project permission
        whenPermission(rolePermissionsHelper).deny(user2, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.deleteOrDeactivateProjectRole(user2, projectRole.getIdentity()))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        whenPermission(rolePermissionsHelper).allow(user, projectRole.getProject(), ProjectPermissionType.EDIT_PROJECT_USERS);

        assertThatThrownBy(() -> projectUsersService.deleteOrDeactivateProjectRole(user, projectRole.getIdentity()))
                .isInstanceOf(NoPermissionException.class);
    }

    Role getUserRole() {
        return rolesRepository.findOneByCode("project_user");
    }

    Role getAdminRole() {
        return rolesRepository.findOneByCode(ProjectRoleType.ADMIN.getCode());
    }

    ProjectRoleInfo correctRole(Role role) {
        return correctRole(role.getId());
    }

    ProjectRoleInfo correctRole(Long roleId) {
        ProjectRoleInfo projectRoleInfo = new ProjectRoleInfo();
        projectRoleInfo.setRoleId(roleId);
        return projectRoleInfo;
    }
}
