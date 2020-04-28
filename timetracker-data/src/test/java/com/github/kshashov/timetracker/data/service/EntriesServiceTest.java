package com.github.kshashov.timetracker.data.service;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseActionTest;
import com.github.kshashov.timetracker.data.entity.Entry;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntriesServiceTest extends BaseActionTest {

    @Autowired
    EntriesService entriesService;

    @Autowired
    EntriesRepository entriesRepository;

    @MockBean
    RolePermissionsHelper rolePermissionsHelper;

    //
    // CREATE
    //

    @Test
    void createEntry_Ok() {
        Entry entry = correctEntry();
        Entry result = entriesService.createEntry(correctEntry());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAction().getId()).isEqualTo(entry.getAction().getId());
        assertThat(result.getUser().getId()).isEqualTo(entry.getUser().getId());
        assertThat(result.getHours()).isEqualTo(entry.getHours());
        assertThat(result.getIsClosed()).isEqualTo(entry.getIsClosed());
        assertThat(result.getObs()).isEqualTo(entry.getObs());
        assertThat(result.getTitle()).isEqualTo(entry.getTitle());
    }

    @Test
    void createEntry_IncorrectEntry_IncorrectArgumentException() {

        // Hours
        Entry entry0 = correctEntry();
        entry0.setHours(0.0);

        assertThatThrownBy(() -> entriesService.createEntry(entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        entry0.setHours(null);

        assertThatThrownBy(() -> entriesService.createEntry(entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        // State
        Entry entry1 = correctEntry();
        entry1.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.createEntry(entry1))
                .isInstanceOf(IncorrectArgumentException.class);

        // Title
        Entry entry2 = correctEntry();
        entry2.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.createEntry(entry2))
                .isInstanceOf(IncorrectArgumentException.class);

        // Obs
        Entry entry3 = correctEntry();
        entry3.setObs(null);

        assertThatThrownBy(() -> entriesService.createEntry(entry3))
                .isInstanceOf(IncorrectArgumentException.class);

        // Action
        Entry entry4 = correctEntry();
        entry4.setAction(null);

        assertThatThrownBy(() -> entriesService.createEntry(entry4))
                .isInstanceOf(IncorrectArgumentException.class);

        // User
        Entry entry5 = correctEntry();
        entry5.setUser(null);

        assertThatThrownBy(() -> entriesService.createEntry(entry5))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // CREATE BY USER
    //

    @Test
    @Sql("classpath:tests/EntriesServiceTest.createEntry_UserHasNoPermission.sql")
    void createEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("createEntry_UserHasNoPermission");
        User user = getUser();

        // Same user
        Entry entry0 = correctEntry();
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.createEntry(user, entry0))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        Entry entry1 = correctEntry();
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.createEntry(user2, entry1))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/EntriesServiceTest.createEntry_CorrectUser.sql")
    void createEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("createEntry_CorrectUser");
        User user = getUser();

        // Same user
        Entry entry0 = correctEntry();

        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(true);

        Entry result = entriesService.createEntry(user, entry0);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        // Other user
        Entry entry1 = correctEntry();

        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(true);

        result = entriesService.createEntry(user2, entry1);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    //
    // UPDATE
    //

    @Test
    void updateEntry_Ok() {
        Entry entry = entriesService.createEntry(correctEntry());
        entry.setTitle("updateEntry_Ok");
        entry.setHours(10.5);
        entry.setObs(LocalDate.of(2020, 2, 7));

        Entry result = entriesService.updateEntry(entry);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAction().getId()).isEqualTo(entry.getAction().getId());
        assertThat(result.getUser().getId()).isEqualTo(entry.getUser().getId());
        assertThat(result.getHours()).isEqualTo(entry.getHours());
        assertThat(result.getIsClosed()).isEqualTo(entry.getIsClosed());
        assertThat(result.getObs()).isEqualTo(entry.getObs());
        assertThat(result.getTitle()).isEqualTo(entry.getTitle());
    }

    @Test
    void updateEntry_IncorrectEntry_IncorrectArgumentException() {

        // Hours
        Entry entry0 = entriesService.createEntry(correctEntry());
        entry0.setHours(0.0);

        assertThatThrownBy(() -> entriesService.updateEntry(entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        entry0.setHours(null);

        assertThatThrownBy(() -> entriesService.updateEntry(entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        // State
        Entry entry1 = entriesService.createEntry(correctEntry());
        entry1.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.updateEntry(entry1))
                .isInstanceOf(IncorrectArgumentException.class);

        // Title
        Entry entry2 = entriesService.createEntry(correctEntry());
        entry2.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.updateEntry(entry2))
                .isInstanceOf(IncorrectArgumentException.class);

        // Obs
        Entry entry3 = entriesService.createEntry(correctEntry());
        entry3.setObs(null);

        assertThatThrownBy(() -> entriesService.updateEntry(entry3))
                .isInstanceOf(IncorrectArgumentException.class);

        // Action
        Entry entry4 = entriesService.createEntry(correctEntry());
        entry4.setAction(null);

        assertThatThrownBy(() -> entriesService.updateEntry(entry4))
                .isInstanceOf(IncorrectArgumentException.class);

        // User
        Entry entry5 = entriesService.createEntry(correctEntry());
        entry5.setUser(null);

        assertThatThrownBy(() -> entriesService.updateEntry(entry5))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // UPDATE BY USER
    //

    @Test
    @Sql("classpath:tests/EntriesServiceTest.updateEntry_UserHasNoPermission.sql")
    void updateEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("updateEntry_UserHasNoPermission");
        User user = getUser();

        // Same user
        Entry entry0 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.updateEntry(user, entry0))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        Entry entry1 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.updateEntry(user2, entry1))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/EntriesServiceTest.updateEntry_CorrectUser.sql")
    void updateEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("updateEntry_CorrectUser");
        User user = getUser();

        // Same user
        Entry entry0 = entriesService.createEntry(correctEntry());

        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(true);

        Entry result = entriesService.updateEntry(user, entry0);
        assertThat(result).isNotNull();

        // Other user
        Entry entry1 = entriesService.createEntry(correctEntry());

        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(true);

        result = entriesService.updateEntry(user2, entry1);
        assertThat(result).isNotNull();
    }

    //
    // DELETE
    //

    @Test
    void deleteEntry_Ok() {
        Entry entry = entriesService.createEntry(correctEntry());

        assertThat(entriesRepository.existsById(entry.getId())).isTrue();
        entriesService.deleteEntry(entry.getId());
        assertThat(entriesRepository.existsById(entry.getId())).isFalse();
    }

    @Test
    void deleteEntry_IncorrectEntry_IncorrectArgumentException() {

        // Closed entry
        Entry entry = entriesService.createEntry(correctEntry());
        entry.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.deleteEntry(entry.getId()))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // DELETE BY USER
    //

    @Test
    @Sql("classpath:tests/EntriesServiceTest.deleteEntry_CorrectUser.sql")
    void deleteEntry_CorrectUser_Ok() {
        User user2 = getUsersRepository().findOneByEmail("deleteEntry_CorrectUser");
        User user = getUser();

        // Same user
        Entry entry0 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(true);

        assertThat(entriesRepository.existsById(entry0.getId())).isTrue();
        entriesService.deleteEntry(user, entry0.getId());
        assertThat(entriesRepository.existsById(entry0.getId())).isFalse();

        // Other user
        Entry entry1 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(true);

        assertThat(entriesRepository.existsById(entry1.getId())).isTrue();
        entriesService.deleteEntry(user2, entry1.getId());
        assertThat(entriesRepository.existsById(entry1.getId())).isFalse();
    }

    @Test
    @Sql("classpath:tests/EntriesServiceTest.deleteEntry_UserHasNoPermission.sql")
    void deleteEntry_UserHasNoPermission_NoPermissionException() {
        User user2 = getUsersRepository().findOneByEmail("deleteEntry_UserHasNoPermission");
        User user = getUser();

        // Same user
        Entry entry0 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user), eq(entry0.getAction().getProject()), eq(ProjectPermissionType.EDIT_MY_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.deleteEntry(user, entry0.getId()))
                .isInstanceOf(NoPermissionException.class);

        // Other user
        Entry entry1 = entriesService.createEntry(correctEntry());
        when(rolePermissionsHelper.hasProjectPermission(eq(user2), eq(entry1.getAction().getProject()), eq(ProjectPermissionType.EDIT_LOGS)))
                .thenReturn(false);

        assertThatThrownBy(() -> entriesService.deleteEntry(user2, entry1.getId()))
                .isInstanceOf(NoPermissionException.class);
    }

    Entry correctEntry() {
        Entry entry = new Entry();
        entry.setIsClosed(false);
        entry.setUser(getUser());
        entry.setAction(getAction());
        entry.setObs(LocalDate.of(2020, 2, 10));
        entry.setTitle("Blabla");
        entry.setHours(7.5);

        return entry;
    }
}
