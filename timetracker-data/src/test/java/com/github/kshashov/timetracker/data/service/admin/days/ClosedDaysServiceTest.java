package com.github.kshashov.timetracker.data.service.admin.days;

import com.github.kshashov.timetracker.data.BaseProjectTest;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.repo.ClosedDaysRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClosedDaysServiceTest extends BaseProjectTest {

    @Autowired
    private ClosedDaysService closedDaysService;
    @Autowired
    private ClosedDaysRepository closedDaysRepository;

    //
    // OPEN
    //

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok0() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // From > to
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 5);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok1() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // -[-++++-]-
        LocalDate from = LocalDate.of(2020, 1, 2);
        LocalDate to = LocalDate.of(2020, 1, 17);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok2() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // -[-++]++--
        LocalDate from = LocalDate.of(2020, 1, 2);
        LocalDate to = LocalDate.of(2020, 1, 10);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(5);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok3() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --++[++-]-
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(5);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok4() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --+[++]+--
        LocalDate from = LocalDate.of(2020, 1, 7);
        LocalDate to = LocalDate.of(2020, 1, 12);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(5);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(0);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.openDays.sql")
    void openDays_Ok5() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --++++-[--]-
        LocalDate from = LocalDate.of(2020, 1, 17);
        LocalDate to = LocalDate.of(2020, 1, 20);
        closedDaysService.openDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(0);
    }

    //
    // CLOSE
    //

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok0() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // From > to
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 5);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok1() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // -[-++++-]-
        LocalDate from = LocalDate.of(2020, 1, 2);
        LocalDate to = LocalDate.of(2020, 1, 17);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(16);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(16);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok2() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // -[-++]++--
        LocalDate from = LocalDate.of(2020, 1, 2);
        LocalDate to = LocalDate.of(2020, 1, 10);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(14);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(9);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok3() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --++[++-]-
        LocalDate from = LocalDate.of(2020, 1, 10);
        LocalDate to = LocalDate.of(2020, 1, 17);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(13);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(8);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok4() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --+[++]+--
        LocalDate from = LocalDate.of(2020, 1, 7);
        LocalDate to = LocalDate.of(2020, 1, 12);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(6);
    }

    @Test
    @Sql("classpath:tests/ClosedDaysServiceTest.closeDays.sql")
    void closeDays_Ok5() {
        Project project = getProject();

        // Days 2020.1.5-2020.1.15
        LocalDate allFrom = LocalDate.of(2020, 1, 1);
        LocalDate allTo = LocalDate.of(2020, 2, 1);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(11);

        // --++++-[--]-
        LocalDate from = LocalDate.of(2020, 1, 17);
        LocalDate to = LocalDate.of(2020, 1, 20);
        closedDaysService.closeDays(project.getId(), from, to);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, allFrom, allTo))
                .isEqualTo(15);
        assertThat(closedDaysRepository.countByProjectAndIdentityObsBetween(project, from, to))
                .isEqualTo(4);
    }
}
