package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static com.github.kshashov.timetracker.data.PermissionsMock.whenPermission;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizedProjectsServiceTest extends BaseUserTest {
    @MockBean
    private RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    private AuthorizedProjectsService projectsService;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private ProjectRolesRepository projectRolesRepository;
    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;

    //
    // CREATE BY USER
    //

    @Test
    void createProject_Ok() {
        assertThat(projectsRepository.existsByTitle("createProject")).isFalse();

        ProjectInfo projectInfo = correctProjectInfo("createProject");

        // Create project
        Project result = projectsService.createProject(getUser(), projectInfo);

        assertThat(projectsRepository.existsByTitle("createProject")).isTrue();
        result = projectsRepository.findById(result.getId()).get();

        // Check user role
        ProjectRole projectRole = projectRolesRepository.findOneByUserIdAndProjectId(getUser().getId(), result.getId());
        assertThat(projectRole).isNotNull();
        assertThat(projectRole.getRole().getCode()).isEqualTo(ProjectRoleType.ADMIN.getCode());
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateProject_CorrectUser_Ok() {
        User user = getUser();
        Project project = projectsService.createProject(getUser(), correctProjectInfo("updateProject_CorrectUser_0"));
        ProjectInfo projectInfo = correctProjectInfo("updateProject_CorrectUser_1");

        // Make user to has EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        // Update project
        projectsService.updateProject(user, project.getId(), projectInfo);
        Project result = projectsRepository.findById(project.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_CorrectUser_1");
        assertThat(result.getActions()).isEqualTo(project.getActions());

    }

    @Test
    void updateProject_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = projectsService.createProject(getUser(), correctProjectInfo("updateProject_UserHasNoPermission"));
        ProjectInfo projectInfo = correctProjectInfo("updateProject_UserHasNoPermission");
        // Make user to has no EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        assertThatThrownBy(() -> projectsService.updateProject(user, project.getId(), projectInfo))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // ACTIVATE BY USER
    //

    @Test
    void activateProject_CorrectUser_Ok() {
        User user = getUser();

        // Prepare inactive project
        Project project = projectsService.createProject(getUser(), correctProjectInfo("activateProject_CorrectUser_Ok"));
        assertThat(project).isNotNull();
        project.setIsActive(false);
        projectsRepository.save(project);

        project = projectsRepository.findById(project.getId()).get();

        assertThat(project).isNotNull();
        assertThat(project.getIsActive()).isFalse();

        // Make user to has EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        // Activate
        projectsService.activateProject(user, project.getId());

        project = projectsRepository.findById(project.getId()).get();

        assertThat(project).isNotNull();
        assertThat(project.getIsActive()).isTrue();
    }


    @Test
    void activateProject_UserHasNoPermission_NoPermissionException() {
        User user = getUser();

        // Prepare inactive projects
        Project project = projectsService.createProject(user, correctProjectInfo("deleteOrDeactivateProject_UserHasNoPermission"));
        assertThat(project).isNotNull();
        project.setIsActive(false);
        projectsRepository.save(project);

        project = projectsRepository.findById(project.getId()).get();

        assertThat(project).isNotNull();
        assertThat(project.getIsActive()).isFalse();

        // Make user to has no EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        Project finalProject = project;
        assertThatThrownBy(() -> projectsService.activateProject(getUser(), finalProject.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // DELETE BY USER
    //

    @Test
    void deleteOrDeactivateProject_CorrectUser_Ok() {
        User user = getUser();
        Project project = projectsService.createProject(user, correctProjectInfo("deleteOrDeactivateProject_CorrectUser"));

        // Make user to has EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        // Project exists and has no entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(0);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(user, project.getId());

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsByTitle("deleteOrDeactivateProject_CorrectUser")).isFalse();
        assertThat(projectRolesRepository.findWithUserByProjectOrderByUserName(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findByActionProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findWithProjectByProjectOrderByTitleAsc(project).size()).isEqualTo(0);
    }

    @Test
    void deleteOrDeactivateProject_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = projectsService.createProject(user, correctProjectInfo("deleteOrDeactivateProject_UserHasNoPermission"));

        // Make user to has no EDIT_PROJECT_INFO project permission
        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_PROJECT_INFO);

        assertThatThrownBy(() -> projectsService.deleteOrDeactivateProject(getUser(), project.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    ProjectInfo correctProjectInfo(String title) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setTitle(title);
        return projectInfo;
    }
}
