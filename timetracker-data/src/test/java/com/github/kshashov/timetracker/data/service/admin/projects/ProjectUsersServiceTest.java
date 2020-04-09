package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.ProjectRoleIdentity;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.RolesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectUsersServiceTest extends BaseProjectTest {
    @MockBean
    RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    protected RolesRepository rolesRepository;
    @Autowired
    private ProjectUsersService projectUsersService;
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

    @Test
    void createRole_Ok() {
        assertThat(projectRolesRepository.existsByUserAndProject(getUser(), getProject())).isFalse();

        ProjectRole projectRole = correctRole(getUserRole());

        // Create project role
        projectUsersService.createProjectRole(projectRole);

        assertThat(projectRolesRepository.existsByUserAndProject(getUser(), getProject())).isTrue();
        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), getProject().getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRole.getRole().getId());
    }

    @Test
    void createRole_ProjectRoleIdIsNotNull_IllegalArgumentException() {
        ProjectRole projectRole = correctRole(getUserRole());
        projectRole.setIdentity(new ProjectRoleIdentity());

        assertThatThrownBy(() -> projectUsersService.createProjectRole(projectRole))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRole_ProjectUserAlreadyExist_IncorrectArgumentException() {
        // Create
        projectUsersService.createProjectRole(correctRole(getAdminRole()));
        // Create again
        assertThatThrownBy(() -> projectUsersService.createProjectRole(correctRole(getUserRole())))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createRole_IncorrectProjectRole_NullPointerException() {
        // Empty project
        ProjectRole projectRole0 = correctRole(getUserRole());
        projectRole0.setProject(null);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(projectRole0))
                .isInstanceOf(NullPointerException.class);

        // Empty user
        ProjectRole projectRole1 = correctRole(getUserRole());
        projectRole1.setUser(null);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(projectRole1))
                .isInstanceOf(NullPointerException.class);

        // Empty role
        ProjectRole projectRole2 = correctRole(getUserRole());
        projectRole2.setRole(null);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(projectRole2))
                .isInstanceOf(NullPointerException.class);
    }

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
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        // Create
        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isFalse();

        ProjectRole projectRole = correctRole(getUserRole());
        projectUsersService.createProjectRole(user2, projectRole);

        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isTrue();
        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(user.getId(), project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRole.getRole().getId());
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.createRole_UserHasNoPermission.sql")
    void createRole_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("createRole_UserHasNoPermission");
        User user = getUser();
        Project project = getProject();

        ProjectRole projectRole = correctRole(getUserRole());

        // Make user to has no EDIT_PROJECT_USERS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(false);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(user2, projectRole))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(user, projectRole))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateRole_IncorrectAction_NullPointerException() {
        // Empty project
        ProjectRole projectRole0 = projectUsersService.createProjectRole(correctRole(getUserRole()));
        projectRole0.setProject(null);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(projectRole0))
                .isInstanceOf(NullPointerException.class);

        // Empty user
        ProjectRole projectRole1 = projectRolesRepository.findById(projectRole0.getIdentity()).get();
        projectRole1.setUser(null);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(projectRole1))
                .isInstanceOf(NullPointerException.class);

        // Empty role
        ProjectRole projectRole2 = projectRolesRepository.findById(projectRole0.getIdentity()).get();
        projectRole2.setRole(null);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(projectRole2))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRole_ProjectRoleIdIsNull_NullPointerException() {
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));
        projectRole.setIdentity(null);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(projectRole))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRole_Ok() {
        // Create
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));

        // Update
        projectRole.setRole(getAdminRole());
        projectUsersService.updateProjectRole(projectRole);

        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), getProject().getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRole.getRole().getId());
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
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));

        // Make user to has EDIT_PROJECT_USERS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(projectRole.getProject()), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        // Update role
        projectRole.setRole(getAdminRole());
        projectUsersService.updateProjectRole(user2, projectRole);

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

        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));
        projectRole.setRole(getAdminRole());

        // Make user to has no EDIT_PROJECT_USERS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(false);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(user2, projectRole))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(user, projectRole))
                .isInstanceOf(NoPermissionException.class);

    }

    //
    // DELETE 
    //

    @Test
    void deleteOrDeactivateProjectRole_NoEntries_ReturnsTrue() {
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_NoClosedEntries.sql")
    void deleteOrDeactivateProjectRole_NoClosedEntries_ReturnsTrue() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProjectRole_NoClosedEntries");
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), project.getId());

        // Project role exists and has open entries and actions
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(2);


        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_HasClosedEntries.sql")
    void deleteOrDeactivateProjectRole_HasClosedEntries_ReturnsFalse() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProjectRole_HasClosedEntries");
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), project.getId());

        // Project role exists and has entries and actions
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(3);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deactivated
        assertThat(isDeleted).isFalse();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isTrue();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isTrue();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(2);
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).stream()
                .allMatch(Entry::getIsClosed)
        ).isTrue();
    }

    //
    // DELETE BY USER
    //

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_CorrectUser.sql")
    void deleteOrDeactivateProjectRole_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("deleteOrDeactivateProjectRole_CorrectUser");
        User user = getUser();
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);

        // Make user to has EDIT_PROJECT_USERS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(projectRole.getIdentity().getProjectId()), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(user2, projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_UserHasNoPermission.sql")
    void deleteOrDeactivateProjectRole_UserHasNoPermission_ExceptionThrown() {
        User user2 = getUsersRepository().findOneByEmail("deleteOrDeactivateProjectRole_UserHasNoPermission");
        User user = getUser();
        ProjectRole projectRole = projectUsersService.createProjectRole(correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findByUserIdAndActionProjectId(projectRole.getUser().getId(), projectRole.getProject().getId()).size()).isEqualTo(0);

        // Make user to has no EDIT_PROJECT_USERS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(projectRole.getProject()), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(false);

        assertThatThrownBy(() -> projectUsersService.deleteOrDeactivateProjectRole(user2, projectRole.getIdentity()))
                .isInstanceOf(NoPermissionException.class);

        // Check with the same user
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(getProject()), eq(ProjectPermissionType.EDIT_PROJECT_USERS)))
                .thenReturn(true);

        assertThatThrownBy(() -> projectUsersService.deleteOrDeactivateProjectRole(user, projectRole.getIdentity()))
                .isInstanceOf(NoPermissionException.class);
    }

    Role getUserRole() {
        return rolesRepository.findOneByCode("project_user");
    }

    Role getAdminRole() {
        return rolesRepository.findOneByCode(ProjectRoleType.ADMIN.getCode());
    }

    ProjectRole correctRole(Role role) {
        return correctRole(role, getProject());
    }

    ProjectRole correctRole(Role role, Project project) {
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setUser(getUser());
        projectRole.setRole(role);
        return projectRole;
    }
}