package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseActionTest;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static com.github.kshashov.timetracker.data.PermissionsMock.whenPermission;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizedEntriesServiceTest extends BaseActionTest {

    @Autowired
    AuthorizedEntriesService entriesService;

    @Autowired
    EntriesRepository entriesRepository;

    @MockBean
    RolePermissionsHelper rolePermissionsHelper;

    //
    // CREATE BY USER
    //

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.createEntry_UserHasNoPermission.sql")
    void createEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("createEntry_UserHasNoPermission");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        EntryInfo entry0 = correctEntry();
        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_MY_LOGS);

        assertThatThrownBy(() -> entriesService.createEntry(user, user.getId(), entry0))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        EntryInfo entry1 = correctEntry();
        whenPermission(rolePermissionsHelper).deny(user2, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        assertThatThrownBy(() -> entriesService.createEntry(user2, user.getId(), entry1))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.createEntry_CorrectUser.sql")
    void createEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("createEntry_CorrectUser");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        EntryInfo entry0 = correctEntry();

        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry result = entriesService.createEntry(user, user.getId(), entry0);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        // Other user
        EntryInfo entry1 = correctEntry();

        whenPermission(rolePermissionsHelper).allow(user2, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
        result = entriesService.createEntry(user2, user.getId(), entry1);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    //
    // UPDATE BY USER
    //

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.u" +
            "pdateEntry_UserHasNoPermission.sql")
    void updateEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("updateEntry_UserHasNoPermission");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry0 = entriesService.createEntry(user, user.getId(), correctEntry());

        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        assertThatThrownBy(() -> entriesService.updateEntry(user, entry0.getId(), correctEntry()))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry1 = entriesService.createEntry(user, user.getId(), correctEntry());

        whenPermission(rolePermissionsHelper).deny(user2, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
        assertThatThrownBy(() -> entriesService.updateEntry(user2, entry1.getId(), correctEntry()))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.updateEntry_CorrectUser.sql")
    void updateEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("updateEntry_CorrectUser");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry0 = entriesService.createEntry(user, user.getId(), correctEntry());

        Entry result = entriesService.updateEntry(user, entry0.getId(), correctEntry());
        assertThat(result).isNotNull();

        // Other user
        Entry entry1 = entriesService.createEntry(user, user.getId(), correctEntry());

        whenPermission(rolePermissionsHelper).allow(user2, project, ProjectPermissionType.VIEW_PROJECT_LOGS);
        result = entriesService.updateEntry(user2, entry1.getId(), correctEntry());
        assertThat(result).isNotNull();
    }

    //
    // DELETE BY USER
    //

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.deleteEntry_CorrectUser.sql")
    void deleteEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("deleteEntry_CorrectUser");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry0 = entriesService.createEntry(user, user.getId(), correctEntry());

        assertThat(entriesRepository.existsById(entry0.getId())).isTrue();
        entriesService.deleteEntry(user, entry0.getId());
        assertThat(entriesRepository.existsById(entry0.getId())).isFalse();

        // Other user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry1 = entriesService.createEntry(user, user.getId(), correctEntry());

        whenPermission(rolePermissionsHelper).allow(user2, entry1.getAction().getProject(), ProjectPermissionType.VIEW_PROJECT_LOGS);
        assertThat(entriesRepository.existsById(entry1.getId())).isTrue();
        entriesService.deleteEntry(user2, entry1.getId());
        assertThat(entriesRepository.existsById(entry1.getId())).isFalse();
    }

    @Test
    @Sql("classpath:tests/AuthorizedEntriesServiceTest.deleteEntry_UserHasNoPermission.sql")
    void deleteEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("deleteEntry_UserHasNoPermission");
        User user = getUser();
        Project project = getAction().getProject();

        // Same user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry0 = entriesService.createEntry(user, user.getId(), correctEntry());

        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        assertThatThrownBy(() -> entriesService.deleteEntry(user, entry0.getId()))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.EDIT_MY_LOGS);
        Entry entry1 = entriesService.createEntry(user, user.getId(), correctEntry());
        whenPermission(rolePermissionsHelper).deny(user2, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        assertThatThrownBy(() -> entriesService.deleteEntry(user2, entry1.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    EntryInfo correctEntry() {
        EntryInfo entry = new EntryInfo();
        entry.setActionId(getAction().getId());
        entry.setObs(LocalDate.of(2020, 2, 10));
        entry.setTitle("Blabla");
        entry.setHours(7.5);

        return entry;
    }
}
