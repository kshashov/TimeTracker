package com.github.kshashov.timetracker.data.service.admin.projects;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectActionsServiceTest extends BaseProjectTest {
    @MockBean
    RolePermissionsHelper rolePermissionsHelper;

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
    void createAction_Ok() {
        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "createAction")).isFalse();

        Action action = correctAction("createAction");

        // Create action
        Action result = actionsService.createAction(action);

        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "createAction")).isTrue();
        result = actionsRepository.getOne(result.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("createAction");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void createAction_ActionIdIsNotNull_IllegalArgumentException() {
        Action action = correctAction("createAction_ActionIdIsNotNull");
        action.setId(0L);

        assertThatThrownBy(() -> actionsService.createAction(action))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createAction_ActionTitleAlreadyExist_IncorrectArgumentException() {
        // Create
        actionsService.createAction(correctAction("createAction_ActionTitleAlreadyExist"));
        // Create again
        assertThatThrownBy(() -> actionsService.createAction(correctAction("createAction_ActionTitleAlreadyExist")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createAction_IncorrectAction_IncorrectArgumentException() {
        // Empty title
        Action action0 = correctAction("");

        assertThatThrownBy(() -> actionsService.createAction(action0))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        Action action1 = correctAction("");
        Boolean projectActive = action1.getProject().getIsActive();
        action1.getProject().setIsActive(false);

        assertThatThrownBy(() -> actionsService.createAction(action1))
                .isInstanceOf(IncorrectArgumentException.class);

        action1.getProject().setIsActive(projectActive);
    }

    @Test
    void createAction_ActiveFalse_OkWithActiveTrue() {
        Action action = correctAction("createAction_ActiveFalse");
        action.setIsActive(false);

        Action result = actionsService.createAction(action);
        assertThat(result.getIsActive()).isEqualTo(true);
    }

    //
    // CREATE BY USER
    //

    @Test
    void createAction_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(true);

        assertThat(actionsRepository.existsByProjectAndTitle(project, "createAction_CorrectUser")).isFalse();

        // Create
        Action action = actionsService.createAction(user, correctAction("createAction_CorrectUser"));
        assertThat(action).isNotNull();
        assertThat(action.getId()).isNotNull();
        assertThat(action.getTitle()).isEqualTo("createAction_CorrectUser");
        assertThat(action.getIsActive()).isTrue();
        assertThat(actionsRepository.existsByProjectAndTitle(project, "createAction_CorrectUser")).isTrue();
    }

    @Test
    void createAction_UserHasNoPermission_NoPermissionException() {
        User user = getUser();

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(getProject()), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(false);

        assertThatThrownBy(() -> actionsService.createAction(getUser(), correctAction("createAction_UserHasNoPermission")))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateAction_ActionTitleAlreadyExist_IncorrectArgumentException() {
        // Create first
        actionsService.createAction(correctAction("updateAction_ActionTitleAlreadyExist_0"));
        // Create second
        Action result = actionsService.createAction(correctAction("updateAction_ActionTitleAlreadyExist_1"));
        // Try to save with the same title
        result.setTitle("updateAction_ActionTitleAlreadyExist_0");

        assertThatThrownBy(() -> actionsService.updateAction(result))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateAction_IncorrectAction_IncorrectArgumentException() {
        // Empty title
        Action action0 = actionsService.createAction(correctAction("updateAction_IncorrectAction_0"));
        action0.setTitle("");

        assertThatThrownBy(() -> actionsService.updateAction(action0))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive action
        Action action1 = actionsService.createAction(correctAction("updateAction_IncorrectAction_1"));
        action1.setIsActive(false);

        assertThatThrownBy(() -> actionsService.updateAction(action1))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        Action action2 = actionsService.createAction(correctAction("updateAction_IncorrectAction_2"));
        Boolean projectActive = action2.getProject().getIsActive();
        action2.getProject().setIsActive(false);

        assertThatThrownBy(() -> actionsService.updateAction(action2))
                .isInstanceOf(IncorrectArgumentException.class);

        action2.getProject().setIsActive(projectActive);
    }

    @Test
    void updateAction_ActionIdIsNull_NullPointerException() {
        Action action = actionsService.createAction(correctAction("updateAction_ActionIdIsNull"));
        action.setId(null);

        assertThatThrownBy(() -> actionsService.updateAction(action))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAction_Ok() {
        Action action = actionsService.createAction(correctAction("updateAction_0"));
        action.setTitle("updateAction_1");

        actionsService.updateAction(action);
        Action result = actionsRepository.getOne(action.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(action.getId());
        assertThat(result.getTitle()).isEqualTo("updateAction_1");
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateAction_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        Action action = actionsService.createAction(correctAction("updateAction_CorrectUser_Ok_0"));
        action.setTitle("updateAction_CorrectUser_Ok_1");

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(project), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(true);

        // Update project
        actionsService.updateAction(user, action);
        Action result = actionsRepository.getOne(action.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(action.getId());
        assertThat(result.getTitle()).isEqualTo("updateAction_CorrectUser_Ok_1");
    }

    @Test
    void updateAction_UserHasNoPermission_NoPermissionException() {
        Action action = actionsService.createAction(correctAction("updateAction_UserHasNoPermission_NoPermissionException"));

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(getUser()), eq(getProject()), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(false);

        assertThatThrownBy(() -> actionsService.updateAction(getUser(), action))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // DELETE
    //

    @Test
    void deleteOrDeactivateAction_IncorrectAction_ExceptionThrown() {
        // Inactive action
        Action action = actionsService.createAction(correctAction("deleteOrDeactivateAction_IncorrectAction_ExceptionThrown"));
        action.setIsActive(false);

        assertThatThrownBy(() -> actionsService.deleteOrDeactivateAction(action.getId()))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        Boolean projectActive = action.getProject().getIsActive();
        action.getProject().setIsActive(false);

        assertThatThrownBy(() -> actionsService.deleteOrDeactivateAction(action.getId()))
                .isInstanceOf(IncorrectArgumentException.class);

        action.getProject().setIsActive(projectActive);
    }

    @Test
    void deleteOrDeactivateAction_NoEntries_ReturnsTrue() {
        Action action = actionsService.createAction(correctAction("deleteOrDeactivateAction_NoEntries_ReturnsTrue"));

        // Action exists and has no entries
        assertThat(action).isNotNull();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);

        boolean isDeleted = actionsService.deleteOrDeactivateAction(action.getId());

        // Action is deleted
        assertThat(isDeleted).isTrue();
        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "deleteOrDeactivateAction_NoEntries_ReturnsTrue")).isFalse();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ActionsServiceTest.deleteOrDeactivateAction_NoClosedEntries.sql")
    void deleteOrDeactivateAction_NoClosedEntries_ReturnsTrue() {
        Action action = actionsRepository.findOneByProjectAndTitle(getProject(), "deleteOrDeactivateAction_NoClosedEntries");

        // Action exists and has open entries and actions
        assertThat(action).isNotNull();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(2);

        boolean isDeleted = actionsService.deleteOrDeactivateAction(action.getId());

        // Action is deleted
        assertThat(isDeleted).isTrue();
        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "deleteOrDeactivateAction_NoClosedEntries")).isFalse();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ActionsServiceTest.deleteOrDeactivateAction_HasClosedEntries.sql")
    void deleteOrDeactivateAction_HasClosedEntries_ReturnsFalse() {
        Action action = actionsRepository.findOneByProjectAndTitle(getProject(), "deleteOrDeactivateAction_HasClosedEntries");

        // Action exists and has entries and actions
        assertThat(action).isNotNull();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(3);

        boolean isDeleted = actionsService.deleteOrDeactivateAction(action.getId());

        // Action is deactivated
        assertThat(isDeleted).isFalse();
        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "deleteOrDeactivateAction_HasClosedEntries")).isTrue();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(2);
        assertThat(entriesRepository.findByAction(action).stream()
                .allMatch(Entry::getIsClosed)
        ).isTrue();
    }

    //
    // DELETE BY USER
    //

    @Test
    void deleteOrDeactivateAction_CorrectUser_Ok() {
        User user = getUser();
        Action action = actionsService.createAction(correctAction("deleteOrDeactivateAction_CorrectUser_Ok"));

        // Action exists and has no entries
        assertThat(action).isNotNull();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(getProject()), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(true);

        boolean isDeleted = actionsService.deleteOrDeactivateAction(action.getId());

        // Action is deleted
        assertThat(isDeleted).isTrue();
        assertThat(actionsRepository.existsById(action.getId())).isFalse();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);
    }

    @Test
    void deleteOrDeactivateAction_UserHasNoPermission_ExceptionThrown() {
        User user = getUser();
        Action action = actionsService.createAction(correctAction("deleteOrDeactivateAction_UserHasNoPermission_ExceptionThrown"));

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(getProject()), eq(ProjectPermissionType.EDIT_PROJECT_ACTIONS)))
                .thenReturn(false);

        assertThatThrownBy(() -> actionsService.deleteOrDeactivateAction(getUser(), action.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    Action correctAction(String title) {
        Action action = new Action();
        action.setTitle(title);
        action.setIsActive(true);
        action.setProject(getProject());
        return action;
    }
}
