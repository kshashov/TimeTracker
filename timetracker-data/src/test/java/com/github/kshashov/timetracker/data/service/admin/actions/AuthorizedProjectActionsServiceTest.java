package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.service.admin.projects.ProjectsService;
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
public class AuthorizedProjectActionsServiceTest extends BaseProjectTest {
    @MockBean
    private RolePermissionsHelper rolePermissionsHelper;

    @Autowired
    private ProjectsRepository projectsRepository;
    @Autowired
    private ProjectRolesRepository projectRolesRepository;
    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;
    @Autowired
    private AuthorizedProjectActionsService actionsService;
    @Autowired
    private ProjectsService projectService;

    //
    // CREATE BY USER
    //

    @Test
    void createAction_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        assertThat(actionsRepository.existsByProjectAndTitle(project, "createAction_CorrectUser")).isFalse();

        // Create
        Action action = actionsService.createAction(user, project.getId(), correctAction("createAction_CorrectUser"));
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
        whenPermission(rolePermissionsHelper).deny(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);


        assertThatThrownBy(() -> actionsService.createAction(user, getProject().getId(), correctAction("createAction_UserHasNoPermission")))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateAction_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        Action action = actionsService.createAction(user, project.getId(), correctAction("updateAction_CorrectUser_Ok_0"));
        ActionInfo actionInfo = correctAction("updateAction_CorrectUser_Ok_1");

        // Update project
        actionsService.updateAction(user, action.getId(), actionInfo);
        Action result = actionsRepository.findById(action.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(action.getId());
        assertThat(result.getTitle()).isEqualTo("updateAction_CorrectUser_Ok_1");
    }

    @Test
    void updateAction_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = getProject();

        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        Action action = actionsService.createAction(user, project.getId(), correctAction("updateAction_UserHasNoPermission_0"));

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        assertThatThrownBy(() -> actionsService.updateAction(user, action.getId(), correctAction("updateAction_UserHasNoPermission_1")))
                .isInstanceOf(NoPermissionException.class);
    }

    //
    // ACTIVATE BY USER
    //

    @Test
    void activateAction_CorrectUser_Ok() {
        // Make user to has EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).allow(getUser(), getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        // Prepare inactive action
        Action action = actionsService.createAction(getUser(), getProject().getId(), correctAction("activateAction_CorrectUser_Ok"));
        action.setIsActive(false);
        actionsRepository.save(action);

        action = actionsRepository.findWithProjectById(action.getId());
        assertThat(action).isNotNull();
        assertThat(action.getIsActive()).isFalse();

        // Activate
        actionsService.activateAction(getUser(), action.getId());

        action = actionsRepository.findWithProjectById(action.getId());
        assertThat(action).isNotNull();
        assertThat(action.getIsActive()).isTrue();
    }

    @Test
    void activateAction_UserHasNoPermission_NoPermissionException() {
        User user = getUser();

        // Prepare inactive action
        whenPermission(rolePermissionsHelper).allow(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        Action action = actionsService.createAction(user, getProject().getId(), correctAction("activateAction_UserHasNoPermission"));
        action.setIsActive(false);

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).deny(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        assertThatThrownBy(() -> actionsService.activateAction(user, action.getId()))
                .isInstanceOf(NoPermissionException.class);

        action.setIsActive(true);
    }

    //
    // DELETE BY USER
    //

    @Test
    void deleteOrDeactivateAction_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        // Make user to has EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).allow(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        // Action exists and has no entries
        Action action = actionsService.createAction(user, getProject().getId(), correctAction("deleteOrDeactivateAction_CorrectUser_Ok"));
        assertThat(action).isNotNull();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);

        boolean isDeleted = actionsService.deleteOrDeactivateAction(user, action.getId());

        // Action is deleted
        assertThat(isDeleted).isTrue();
        assertThat(actionsRepository.existsById(action.getId())).isFalse();
        assertThat(entriesRepository.findByAction(action).size()).isEqualTo(0);
    }

    @Test
    void deleteOrDeactivateAction_UserHasNoPermission_ExceptionThrown() {
        User user = getUser();
        Project project = getProject();

        whenPermission(rolePermissionsHelper).allow(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        Action action = actionsService.createAction(user, project.getId(), correctAction("deleteOrDeactivateAction_UserHasNoPermission_ExceptionThrown"));

        // Make user to has no EDIT_PROJECT_ACTIONS project permission
        whenPermission(rolePermissionsHelper).deny(user, getProject(), ProjectPermissionType.EDIT_PROJECT_ACTIONS);

        assertThatThrownBy(() -> actionsService.deleteOrDeactivateAction(user, action.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    ActionInfo correctAction(String title) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setTitle(title);
        return actionInfo;
    }
}
