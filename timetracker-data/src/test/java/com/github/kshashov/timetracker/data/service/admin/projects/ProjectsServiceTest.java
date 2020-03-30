package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.TestsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectsServiceTest extends BaseUserTest {
    @Autowired
    TestsRepository testsRepository;

    @Autowired
    ProjectsService projectsService;

    @Autowired
    ProjectUsersService projectUsersService;

    @Autowired
    private ProjectRolesRepository projectRolesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    RolePermissionsHelper rolePermissionsHelper;

/*    @Test
    void createProject_UserHasNoPermission_ExceptionThrown() {
        assertThatThrownBy(() -> projectsService.createProject(getUser(), correctProject("createProject_UserHasNoPermission_ExceptionThrown_0")))
                .isInstanceOf(NoPermissionException.class);
    }*/

    @Test
    void createProject_Ok() {
        Project project0 = correctProject("createProject_Ok");

        // Create project
        Project result = projectsService.createProject(getUser(), project0);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("createProject_Ok");
        assertThat(result.getIsActive()).isEqualTo(true);
        assertThat(result.getActions()).isNull();

        // Check user role
        Optional<ProjectRole> projectRole = projectRolesRepository.findFirstByUserIdAndProjectId(getUser().getId(), result.getId());
        assertThat(projectRole.isPresent()).isEqualTo(true);
        assertThat(projectRole.get().getRole().getCode()).isEqualTo(ProjectRoleType.ADMIN.getCode());
    }

    @Test
    void createProject_ProjectIdIsNotEmpty_ExceptionThrown() {
        Project project0 = correctProject("createProject_ProjectIdIsNotEmpty_ExceptionThrown");
        project0.setId(0L);

        assertThatThrownBy(() -> projectsService.createProject(getUser(), project0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createProject_ProjectTitleAlreadyExist_ExceptionThrown() {
        // Create
        projectsService.createProject(getUser(), correctProject("createProject_ProjectAlreadyExist_ExceptionThrown"));
        // Create again
        assertThatThrownBy(() -> projectsService.createProject(getUser(), correctProject("createProject_ProjectAlreadyExist_ExceptionThrown")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createProject_IncorrectProject_ExceptionThrown() {
        Project project0 = correctProject("");

        assertThatThrownBy(() -> projectsService.createProject(getUser(), project0))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createProject_ActiveFalse_OkWithActiveTrue() {
        Project project0 = correctProject("createProject_ActiveFalse_OkWithActiveTrue");
        project0.setIsActive(false);

        Project result = projectsService.createProject(getUser(), project0);
        assertThat(result.getIsActive()).isEqualTo(true);
    }

    @Test
    void createProject_ActionsNotEmpty_OkWithEmptyActions() {
        Project project0 = correctProject("createProject_ActionsNotEmpty_OkWithEmptyActions");
        project0.setActions(new HashSet<>());
        project0.getActions().add(new Action());

        Project result = projectsService.createProject(getUser(), project0);
        assertThat(result.getActions()).isEmpty();
    }

    //
    // UPDATE
    //

    @Test
    void updateProject_ProjectTitleAlreadyExist_ExceptionThrown() {
        // Create first
        projectsService.createProject(getUser(), correctProject("updateProject_ProjectTitleAlreadyExist_ExceptionThrown_0"));
        // Create second
        Project result = projectsService.createProject(getUser(), correctProject("updateProject_ProjectTitleAlreadyExist_ExceptionThrown_1"));
        // Try to save with the same title
        result.setTitle("updateProject_ProjectTitleAlreadyExist_ExceptionThrown_0");

        assertThatThrownBy(() -> projectsService.updateProject(result))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_IncorrectProject_ExceptionThrown() {
        // Empty title
        Project project0 = projectsService.createProject(getUser(), correctProject("updateProject_IncorrectProject_ExceptionThrown"));
        project0.setTitle("");

        assertThatThrownBy(() -> projectsService.updateProject(project0))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        project0.setIsActive(false);

        assertThatThrownBy(() -> projectsService.updateProject(project0))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_ProjectIdEmpty_ExceptionThrown() {
        Project project0 = correctProject("updateProject_ProjectIdEmpty_ExceptionThrown");
        project0.setId(null);

        assertThatThrownBy(() -> projectsService.updateProject(project0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateProject_Ok() {
        Project project0 = projectsService.createProject(getUser(), correctProject("updateProject_Ok_0"));
        project0.setTitle("updateProject_Ok_0");

        Project result = projectsService.updateProject(project0);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project0.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_Ok_0");
        assertThat(result.getActions()).isEqualTo(project0.getActions());
    }

    //
    // UPDATE BY USER
    //

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Sql("classpath:tests/project_roles.sql")
    void updateProject_CorrectUser_Ok() {
        rolePermissionsHelper.reload();

        Project project = projectsRepository.findById(1L).get();
        project.setTitle("updateProject_CorrectUser_Ok");

        // Get user with EDIT_PROJECT_INFO permission
        User user = usersRepository.findOneByEmail(ProjectPermissionType.EDIT_PROJECT_INFO.getCode());

        Project result = projectsService.updateProject(user, project);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_CorrectUser_Ok");
        assertThat(result.getActions()).isEqualTo(project.getActions());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Sql("classpath:tests/project_roles.sql")
    void updateProject_UserHasNoPermission_ExceptionThrown() {
        Project project = projectsRepository.findById(1L).get();

        assertThatThrownBy(() -> projectsService.updateProject(getUser(), project))
                .isInstanceOf(NoPermissionException.class);
    }

    Project correctProject(String title) {
        Project project = new Project();
        project.setTitle(title);
        project.setIsActive(true);
        return project;
    }
}
