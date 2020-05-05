package com.github.kshashov.timetracker.data.service.admin.actions;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectActionsServiceTest extends BaseProjectTest {

    @Autowired
    private EntriesRepository entriesRepository;
    @Autowired
    private ActionsRepository actionsRepository;
    @Autowired
    private ProjectActionsService actionsService;

    @Test
    void createAction_Ok() {
        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "createAction")).isFalse();

        ActionInfo actionInfo = correctAction("createAction");

        // Create action
        Action result = actionsService.createAction(getProject().getId(), actionInfo);

        assertThat(actionsRepository.existsByProjectAndTitle(getProject(), "createAction")).isTrue();
        result = actionsRepository.findById(result.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("createAction");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void createAction_ActionTitleAlreadyExist_IncorrectArgumentException() {
        // Create
        actionsService.createAction(getProject().getId(), correctAction("createAction_ActionTitleAlreadyExist"));
        // Create again
        assertThatThrownBy(() -> actionsService.createAction(getProject().getId(), correctAction("createAction_ActionTitleAlreadyExist")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void createAction_IncorrectAction_IncorrectArgumentException() {
        // Empty title
        ActionInfo actionInfo = correctAction("");

        assertThatThrownBy(() -> actionsService.createAction(getProject().getId(), actionInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateAction_ActionTitleAlreadyExist_IncorrectArgumentException() {
        // Create first
        actionsService.createAction(getProject().getId(), correctAction("updateAction_ActionTitleAlreadyExist_0"));
        // Create second
        Action result = actionsService.createAction(getProject().getId(), correctAction("updateAction_ActionTitleAlreadyExist_1"));
        // Try to save with the same title
        ActionInfo actionInfo = new ActionInfo("updateAction_ActionTitleAlreadyExist_0");

        assertThatThrownBy(() -> actionsService.updateAction(result.getId(), actionInfo))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateAction_IncorrectAction_IncorrectArgumentException() {
        // Empty title
        Action action0 = actionsService.createAction(getProject().getId(), correctAction("updateAction_IncorrectAction_0"));
        ActionInfo actionInfo0 = correctAction("");

        assertThatThrownBy(() -> actionsService.updateAction(action0.getId(), actionInfo0))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive action
        Action action1 = actionsService.createAction(getProject().getId(), correctAction("updateAction_IncorrectAction_1"));
        action1.setIsActive(false);

        assertThatThrownBy(() -> actionsService.updateAction(action1.getId(), correctAction("updateAction_IncorrectAction_2")))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void updateAction_Ok() {
        Action action = actionsService.createAction(getProject().getId(), correctAction("updateAction_0"));
        ActionInfo actionInfo = correctAction("updateAction_1");

        actionsService.updateAction(action.getId(), actionInfo);
        Action result = actionsRepository.findById(action.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(action.getId());
        assertThat(result.getTitle()).isEqualTo("updateAction_1");
    }

    //
    // ACTIVATE
    //

    @Test
    void activateAction_IncorrectAction_IncorrectArgumentException() {
        // Active action
        Action action0 = actionsService.createAction(getProject().getId(), correctAction("activateAction_IncorrectAction_0"));
        action0.setIsActive(true);

        assertThatThrownBy(() -> actionsService.activateAction(action0.getId()))
                .isInstanceOf(IncorrectArgumentException.class);

        // Inactive project
        Action action_1 = actionsService.createAction(getProject().getId(), correctAction("activateAction_IncorrectAction_1"));
        action_1.getProject().setIsActive(false);

        assertThatThrownBy(() -> actionsService.activateAction(action_1.getId()))
                .isInstanceOf(IncorrectArgumentException.class);

        action_1.getProject().setIsActive(true);
    }

    @Test
    void activateAction_Ok() {
        // Prepare inactive action
        Action action = actionsService.createAction(getProject().getId(), correctAction("activateAction_Ok"));
        action.setIsActive(false);

        action = actionsRepository.findWithProjectById(action.getId());
        assertThat(action).isNotNull();
        assertThat(action.getIsActive()).isFalse();

        // Activate
        actionsService.activateAction(action.getId());
        action = actionsRepository.findWithProjectById(action.getId());
        assertThat(action).isNotNull();
        assertThat(action.getIsActive()).isTrue();
    }

    //
    // DELETE
    //

    @Test
    void deleteOrDeactivateAction_IncorrectAction_ExceptionThrown() {
        // Inactive action
        Action action = actionsService.createAction(getProject().getId(), correctAction("deleteOrDeactivateAction_IncorrectAction_ExceptionThrown"));
        action.setIsActive(false);

        assertThatThrownBy(() -> actionsService.deleteOrDeactivateAction(action.getId()))
                .isInstanceOf(IncorrectArgumentException.class);

        action.setIsActive(true);
    }

    @Test
    void deleteOrDeactivateAction_NoEntries_ReturnsTrue() {
        Action action = actionsService.createAction(getProject().getId(), correctAction("deleteOrDeactivateAction_NoEntries_ReturnsTrue"));

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

    ActionInfo correctAction(String title) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setTitle(title);
        return actionInfo;
    }
}
