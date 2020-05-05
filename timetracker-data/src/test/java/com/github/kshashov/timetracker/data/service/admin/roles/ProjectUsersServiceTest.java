package com.github.kshashov.timetracker.data.service.admin.roles;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class ProjectUsersServiceTest extends BaseProjectTest {
    @MockBean
    private RolePermissionsHelper rolePermissionsHelper;

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

    //
    // CREATE
    //

    @Test
    void createRole_Ok() {
        User user = getUser();
        Project project = getProject();

        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isFalse();

        ProjectRoleInfo projectRole = correctRole(getUserRole());

        // Create project role
        projectUsersService.createProjectRole(project.getId(), user.getId(), projectRole);

        assertThat(projectRolesRepository.existsByUserAndProject(user, project)).isTrue();
        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(user.getId(), project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(getUserRole().getId());
    }

    @Test
    void createRole_ProjectUserAlreadyExist_IncorrectArgumentException() {
        User user = getUser();
        Project project = getProject();

        // Create
        projectUsersService.createProjectRole(project.getId(), user.getId(), correctRole(getAdminRole()));
        // Create again
        assertThatThrownBy(() -> projectUsersService.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole())))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createRole_IncorrectProjectRole_IncorrectArgumentException() {
        User user = getUser();
        Project project = getProject();

        // Empty role
        ProjectRoleInfo projectRoleInfo = correctRole(getUserRole());
        projectRoleInfo.setRoleId(null);

        assertThatThrownBy(() -> projectUsersService.createProjectRole(project.getId(), user.getId(), projectRoleInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateRole_IncorrectAction_IncorrectArgumentException() {
        User user = getUser();
        Project project = getProject();

        // Empty role
        ProjectRole projectRole = projectUsersService.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));
        ProjectRoleInfo projectRoleInfo = correctRole((Long) null);

        assertThatThrownBy(() -> projectUsersService.updateProjectRole(projectRole.getIdentity(), projectRoleInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateRole_Ok() {
        User user = getUser();
        Project project = getProject();

        // Create
        ProjectRole projectRole = projectUsersService.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));

        // Update
        ProjectRoleInfo projectRoleInfo = correctRole(getAdminRole());
        projectUsersService.updateProjectRole(projectRole.getIdentity(), projectRoleInfo);

        ProjectRole result = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), getProject().getId());
        assertThat(result).isNotNull();
        assertThat(result.getRole().getId()).isEqualTo(projectRole.getRole().getId());
    }

    //
    // DELETE 
    //

    @Test
    void deleteOrDeactivateProjectRole_NoEntries_ReturnsTrue() {
        User user = getUser();
        Project project = getProject();

        ProjectRole projectRole = projectUsersService.createProjectRole(project.getId(), user.getId(), correctRole(getUserRole()));

        // Project role exists and has no entries
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_NoClosedEntries.sql")
    void deleteOrDeactivateProjectRole_NoClosedEntries_ReturnsTrue() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProjectRole_NoClosedEntries");
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), project.getId());

        // Project role exists and has open entries and actions
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(2);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isFalse();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isFalse();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectUsersServiceTest.deleteOrDeactivateProjectRole_HasClosedEntries.sql")
    void deleteOrDeactivateProjectRole_HasClosedEntries_ReturnsFalse() {
        Project project = projectsRepository.findOneByTitle("deleteOrDeactivateProjectRole_HasClosedEntries");
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), project.getId());

        // Project role exists and has entries and actions
        assertThat(projectRole).isNotNull();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(3);

        boolean isDeleted = projectUsersService.deleteOrDeactivateProjectRole(projectRole.getIdentity());

        // Project role is deactivated
        assertThat(isDeleted).isFalse();
        assertThat(projectRolesRepository.existsById(projectRole.getIdentity())).isTrue();
        assertThat(projectRolesRepository.existsByUserAndProject(projectRole.getUser(), projectRole.getProject())).isTrue();
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).size()).isEqualTo(2);
        assertThat(entriesRepository.findFullByUserAndActionProject(projectRole.getUser(), projectRole.getProject()).stream()
                .allMatch(Entry::getIsClosed)
        ).isTrue();
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
