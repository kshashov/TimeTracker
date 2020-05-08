package com.github.kshashov.timetracker.data.service.admin.entries;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseActionTest;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
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

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntriesServiceTest extends BaseActionTest {

    @MockBean
    RolePermissionsHelper rolePermissionsHelper;
    @Autowired
    EntriesService entriesService;
    @Autowired
    EntriesRepository entriesRepository;
    @Autowired
    PermissionsRepository permissionsRepository;

    //
    // CREATE
    //

    @Test
    void createEntry_Ok() {
        EntryInfo entryInfo = correctEntry();
        Entry result = entriesService.createEntry(getUser().getId(), entryInfo);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAction().getId()).isEqualTo(entryInfo.getActionId());
        assertThat(result.getUser().getId()).isEqualTo(getUser().getId());
        assertThat(result.getHours()).isEqualTo(entryInfo.getHours());
        assertThat(result.getIsClosed()).isEqualTo(false);
        assertThat(result.getObs()).isEqualTo(entryInfo.getObs());
        assertThat(result.getTitle()).isEqualTo(entryInfo.getTitle());
    }

    @Test
    void createEntry_IncorrectEntry_IncorrectArgumentException() {

        // Hours
        EntryInfo entry0 = correctEntry();
        entry0.setHours(0.0);

        assertThatThrownBy(() -> entriesService.createEntry(getUser().getId(), entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        entry0.setHours(null);

        assertThatThrownBy(() -> entriesService.createEntry(getUser().getId(), entry0))
                .isInstanceOf(IncorrectArgumentException.class);

        // Title
        EntryInfo entry2 = correctEntry();
        entry2.setTitle("");

        assertThatThrownBy(() -> entriesService.createEntry(getUser().getId(), entry2))
                .isInstanceOf(IncorrectArgumentException.class);

        // Obs
        EntryInfo entry3 = correctEntry();
        entry3.setObs(null);

        assertThatThrownBy(() -> entriesService.createEntry(getUser().getId(), entry3))
                .isInstanceOf(IncorrectArgumentException.class);

        // Action
        EntryInfo entry4 = correctEntry();
        entry4.setActionId(null);

        assertThatThrownBy(() -> entriesService.createEntry(getUser().getId(), entry4))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // UPDATE
    //

    @Test
    void updateEntry_Ok() {
        Entry entry = entriesService.createEntry(getUser().getId(), correctEntry());

        EntryInfo entryInfo = correctEntry();
        entryInfo.setTitle("updateEntry_Ok");
        entryInfo.setHours(10.5);
        entryInfo.setObs(LocalDate.of(2020, 2, 7));

        Entry result = entriesService.updateEntry(entry.getId(), entryInfo);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAction().getId()).isEqualTo(entry.getAction().getId());
        assertThat(result.getIsClosed()).isEqualTo(entry.getIsClosed());
        assertThat(result.getUser().getId()).isEqualTo(entry.getUser().getId());

        assertThat(result.getHours()).isEqualTo(entryInfo.getHours());
        assertThat(result.getObs()).isEqualTo(entryInfo.getObs());
        assertThat(result.getTitle()).isEqualTo(entryInfo.getTitle());
    }

    @Test
    void updateEntry_IncorrectEntry_IncorrectArgumentException() {
        User user = getUser();

        // Hours
        Entry entry0 = entriesService.createEntry(user.getId(), correctEntry());

        EntryInfo entryInfo0 = correctEntry();
        entryInfo0.setHours(0.0);
        assertThatThrownBy(() -> entriesService.updateEntry(entry0.getId(), entryInfo0))
                .isInstanceOf(IncorrectArgumentException.class);

        EntryInfo entryInfo1 = correctEntry();
        entryInfo1.setHours(0.0);
        assertThatThrownBy(() -> entriesService.updateEntry(entry0.getId(), entryInfo1))
                .isInstanceOf(IncorrectArgumentException.class);

        // Title
        EntryInfo entryInfo2 = correctEntry();
        entryInfo2.setTitle("");
        assertThatThrownBy(() -> entriesService.updateEntry(entry0.getId(), entryInfo2))
                .isInstanceOf(IncorrectArgumentException.class);

        // Obs

        EntryInfo entryInfo3 = correctEntry();
        entryInfo3.setObs(null);
        assertThatThrownBy(() -> entriesService.updateEntry(entry0.getId(), entryInfo3))
                .isInstanceOf(IncorrectArgumentException.class);

        // Action
        EntryInfo entryInfo4 = correctEntry();
        entryInfo4.setActionId(null);
        assertThatThrownBy(() -> entriesService.updateEntry(entry0.getId(), entryInfo4))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    //
    // OPEN
    //

    @Test
    @Sql("classpath:tests/EntriesServiceTest.openEntries_Ok.sql")
    void openEntries_Ok() {
        Project project = getProject();

        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);

        LocalDate from = LocalDate.of(2020, 1, 6);
        LocalDate to = LocalDate.of(2020, 1, 9);

        Permission selfPermission = permissionsRepository.findOneByCode(ProjectPermissionType.EDIT_MY_LOGS.getCode());

        assertThat(entriesRepository.countByProjectAndUserPermission(project, selfPermission, allFrom, allTo))
                .isEqualTo(6);

        assertThat(entriesRepository.countClosedByProjectAndUserPermission(project, selfPermission, from, to))
                .isEqualTo(2);

        entriesService.openEntries(project.getId(), from, to);

        assertThat(entriesRepository.countByProjectAndUserPermission(project, selfPermission, allFrom, allTo))
                .isEqualTo(6);

        assertThat(entriesRepository.countClosedByProjectAndUserPermission(project, selfPermission, from, to))
                .isEqualTo(0);
    }

    //
    // CLOSE
    //

    @Test
    @Sql("classpath:tests/EntriesServiceTest.closeEntries_Ok.sql")
    void closeEntries_Ok() {
        Project project = getProject();

        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);

        LocalDate from = LocalDate.of(2020, 1, 6);
        LocalDate to = LocalDate.of(2020, 1, 9);

        Permission selfPermission = permissionsRepository.findOneByCode(ProjectPermissionType.EDIT_MY_LOGS.getCode());

        assertThat(entriesRepository.countByProjectAndUserPermission(project, selfPermission, allFrom, allTo))
                .isEqualTo(6);

        assertThat(entriesRepository.countClosedByProjectAndUserPermission(project, selfPermission, from, to))
                .isEqualTo(2);

        entriesService.closeEntries(project.getId(), from, to);

        assertThat(entriesRepository.countByProjectAndUserPermission(project, selfPermission, allFrom, allTo))
                .isEqualTo(6);

        assertThat(entriesRepository.countClosedByProjectAndUserPermission(project, selfPermission, from, to))
                .isEqualTo(4);
    }

    //
    // DELETE
    //

    @Test
    void deleteEntry_Ok() {
        Entry entry = entriesService.createEntry(getUser().getId(), correctEntry());

        assertThat(entriesRepository.existsById(entry.getId())).isTrue();
        entriesService.deleteEntry(entry.getId());
        assertThat(entriesRepository.existsById(entry.getId())).isFalse();
    }

    @Test
    void deleteEntry_IncorrectEntry_IncorrectArgumentException() {

        // Closed entry
        Entry entry = entriesService.createEntry(getUser().getId(), correctEntry());
        entry.setIsClosed(true);

        assertThatThrownBy(() -> entriesService.deleteEntry(entry.getId()))
                .isInstanceOf(IncorrectArgumentException.class);
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
