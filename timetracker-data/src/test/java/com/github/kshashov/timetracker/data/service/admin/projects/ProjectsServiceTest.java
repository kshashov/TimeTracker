package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
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
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectsServiceTest extends BaseUserTest {
    @MockBean
    RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    ProjectsService projectsService;
    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private ProjectRolesRepository projectRolesRepository;
    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;

    @Test
    void createProject_Ok() {
        Project project = correctProject("createProject_Ok");

        // Create project
        Project result = projectsService.createProject(getUser(), project);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("createProject_Ok");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getActions()).isNull();

        // Check user role
        Optional<ProjectRole> projectRole = projectRolesRepository.findFirstByUserIdAndProjectId(getUser().getId(), result.getId());
        assertThat(projectRole.isPresent()).isTrue();
        assertThat(projectRole.get().getRole().getCode()).isEqualTo(ProjectRoleType.ADMIN.getCode());
    }

    @Test
    void createProject_ProjectIdIsNotNull_IllegalArgumentException() {
        Project project = correctProject("createProject_ProjectIdIsNotNull_IllegalArgumentException");
        project.setId(0L);

        assertThatThrownBy(() -> projectsService.createProject(getUser(), project))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createProject_ProjectTitleAlreadyExist_IncorrectArgumentException() {
        // Create
        projectsService.createProject(getUser(), correctProject("createProject_ProjectTitleAlreadyExist_IncorrectArgumentException"));
        // Create again
        assertThatThrownBy(() -> projectsService.createProject(getUser(), correctProject("createProject_ProjectTitleAlreadyExist_IncorrectArgumentException")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createProject_IncorrectProject_IncorrectArgumentException() {
        Project project = correctProject("");

        assertThatThrownBy(() -> projectsService.createProject(getUser(), project))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createProject_ActiveFalse_OkWithActiveTrue() {
        Project project = correctProject("createProject_ActiveFalse_OkWithActiveTrue");
        project.setIsActive(false);

        Project result = projectsService.createProject(getUser(), project);
        assertThat(result.getIsActive()).isEqualTo(true);
    }

    @Test
    void createProject_ActionsNotEmpty_OkWithEmptyActions() {
        Project project = correctProject("createProject_ActionsNotEmpty_OkWithEmptyActions");
        project.setActions(new HashSet<>());
        project.getActions().add(new Action());

        Project result = projectsService.createProject(getUser(), project);
        assertThat(result.getActions()).isEmpty();
    }

    //
    // UPDATE
    //

    @Test
    void updateProject_ProjectTitleAlreadyExist_IncorrectArgumentException() {
        // Create first
        projectsService.createProject(getUser(), correctProject("updateProject_ProjectTitleAlreadyExist_IncorrectArgumentException_0"));
        // Create second
        Project result = projectsService.createProject(getUser(), correctProject("updateProject_ProjectTitleAlreadyExist_IncorrectArgumentException_1"));
        // Try to save with the same title
        result.setTitle("updateProject_ProjectTitleAlreadyExist_IncorrectArgumentException_0");

        assertThatThrownBy(() -> projectsService.updateProject(result))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_IncorrectProject_IncorrectArgumentException() {
        // Empty title
        Project project = projectsService.createProject(getUser(), correctProject("updateProject_IncorrectProject_IncorrectArgumentException"));
        project.setTitle("");

        assertThatThrownBy(() -> projectsService.updateProject(project))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        project.setTitle("updateProject_IncorrectProject_IncorrectArgumentException");
        project.setIsActive(false);

        assertThatThrownBy(() -> projectsService.updateProject(project))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateProject_ProjectIdIsNull_NullPointerException() {
        Project project = correctProject("updateProject_ProjectIdIsNull_NullPointerException");
        project.setId(null);

        assertThatThrownBy(() -> projectsService.updateProject(project))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateProject_Ok() {
        Project project = projectsService.createProject(getUser(), correctProject("updateProject_Ok_0"));
        project.setTitle("updateProject_Ok_1");

        projectsService.updateProject(project);
        Project result = projectsRepository.getOne(project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_Ok_1");
        assertThat(result.getActions()).isEqualTo(project.getActions());
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateProject_CorrectUser_Ok() {
        User user = getUser();
        Project project = projectsService.createProject(getUser(), correctProject("updateProject_CorrectUser_Ok_0"));
        project.setTitle("updateProject_CorrectUser_Ok_1");

        // Make user to has EDIT_PROJECT_INFO project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_INFO)))
                .thenReturn(true);

        // Update project
        projectsService.updateProject(user, project);
        Project result = projectsRepository.getOne(project.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getTitle()).isEqualTo("updateProject_CorrectUser_Ok_1");
        assertThat(result.getActions()).isEqualTo(project.getActions());

    }

    @Test
    void updateProject_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = projectsService.createProject(getUser(), correctProject("updateProject_UserHasNoPermission_NoPermissionException"));

        // Make user to has no EDIT_PROJECT_INFO project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_INFO)))
                .thenReturn(false);

        assertThatThrownBy(() -> projectsService.updateProject(getUser(), project))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // DELETE BY USER
    //

    @Test
    void deleteOrDeactivateProject_ProjectIdIsNull_NullPointerException() {
        Project project = correctProject("updateProject_ProjectIdIsNull_NullPointerException");
        project.setId(null);

        assertThatThrownBy(() -> projectsService.deleteOrDeactivateProject(project))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteOrDeactivateProject_IncorrectProject_IncorrectArgumentException() {
        // Inactive project
        Project project = projectsService.createProject(getUser(), correctProject("updateProject_IncorrectProject_IncorrectArgumentException"));
        project.setIsActive(false);

        assertThatThrownBy(() -> projectsService.deleteOrDeactivateProject(project))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void deleteOrDeactivateProject_NoActions_ReturnsTrue() {
        User user = getUser();
        Project project = projectsService.createProject(user, correctProject("deleteOrDeactivateProject_NoActions_ReturnsTrue"));

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(0);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project);

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsById(1L)).isFalse();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectsServiceTest.deleteOrDeactivateProject_NoClosedEntries.sql")
    void deleteOrDeactivateProject_NoClosedEntries_ReturnsTrue() {
        Project project = projectsRepository.getOne(1L);

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(2);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(2);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project);

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsById(1L)).isFalse();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ProjectsServiceTest.deleteOrDeactivateProject_HasClosedEntries.sql")
    void deleteOrDeactivateProject_HasClosedEntries_ReturnsFalse() {
        Project project = projectsRepository.getOne(1L);

        // Project exists and has entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(2);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(2);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project);

        // Project is deactivated
        assertThat(isDeleted).isFalse();
        assertThat(projectsRepository.existsById(1L)).isTrue();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).stream()
                .allMatch(Entry::getIsClosed)
        ).isTrue();
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(actionsRepository.findAllByProject(project).stream()
                .noneMatch(Action::getIsActive)
        ).isTrue();
    }

    //
    // DELETE BY USER
    //

    @Test
    void deleteOrDeactivateProject_CorrectUser_Ok() {
        User user = getUser();
        Project project = projectsService.createProject(user, correctProject("deleteOrDeactivateProject_CorrectUser_Ok"));

        // Make user to has EDIT_PROJECT_INFO project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_INFO)))
                .thenReturn(true);

        // Project exists and has no entries and actions
        assertThat(project).isNotNull();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(1);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(0);

        boolean isDeleted = projectsService.deleteOrDeactivateProject(project);

        // Project is deleted
        assertThat(isDeleted).isTrue();
        assertThat(projectsRepository.existsById(1L)).isFalse();
        assertThat(projectRolesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(entriesRepository.findAllByProject(project).size()).isEqualTo(0);
        assertThat(actionsRepository.findAllByProject(project).size()).isEqualTo(0);
    }

    @Test
    void deleteOrDeactivateProject_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = projectsService.createProject(user, correctProject("deleteOrDeactivateProject_UserHasNoPermission_NoPermissionException"));

        // Make user to has no EDIT_PROJECT_INFO project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_INFO)))
                .thenReturn(false);

        assertThatThrownBy(() -> projectsService.deleteOrDeactivateProject(getUser(), project))
                .isInstanceOf(NoPermissionException.class);
    }


    Project correctProject(String title) {
        Project project = new Project();
        project.setTitle(title);
        project.setIsActive(true);
        return project;
    }
}
