package com.github.kshashov.timetracker.data.service.admin.days;

import com.github.kshashov.timetracker.core.errors.NoPermissionException;
import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.ClosedDaysRepository;
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
public class AuthorizedClosedDaysServiceTest extends BaseProjectTest {
    @MockBean
    private RolePermissionsHelper rolePermissionsHelper;
    @Autowired
    private AuthorizedClosedDaysService closedDaysService;
    @Autowired
    private ClosedDaysRepository closedDaysRepository;

    //
    // OPEN BY USER
    //

    @Test
    @Sql("classpath:tests/AuthorizedClosedDaysServiceTest.openDays.sql")
    void openDays_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = getProject();
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);

        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        assertThatThrownBy(() -> closedDaysService.openDays(user, project.getId(), from, to))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/AuthorizedClosedDaysServiceTest.openDays.sql")
    void openDays_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        closedDaysService.openDays(user, project.getId(), from, to);

        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(5);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(0);
    }

    //
    // CLOSE BY USER
    //

    @Test
    @Sql("classpath:tests/AuthorizedClosedDaysServiceTest.closeDays.sql")
    void closeDays_UserHasNoPermission_NoPermissionException() {
        User user = getUser();
        Project project = getProject();
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);

        whenPermission(rolePermissionsHelper).deny(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        assertThatThrownBy(() -> closedDaysService.closeDays(user, project.getId(), from, to))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    @Sql("classpath:tests/AuthorizedClosedDaysServiceTest.closeDays.sql")
    void closeDays_CorrectUser_Ok() {
        User user = getUser();
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        whenPermission(rolePermissionsHelper).allow(user, project, ProjectPermissionType.VIEW_PROJECT_LOGS);

        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);
        closedDaysService.closeDays(user, project.getId(), from, to);

        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(13);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(8);
    }
}
