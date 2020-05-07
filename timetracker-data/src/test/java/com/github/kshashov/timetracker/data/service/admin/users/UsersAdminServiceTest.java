package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersAdminServiceTest extends BaseUserTest {

    @Autowired
    UsersAdminService usersService;

    //
    // GET_OR_CREATE
    //

    @Test
    void getOrCreateUser_IncorrectEmail_IncorrectArgumentException() {

        assertThatThrownBy(() -> usersService.getOrCreateUser(null, "name"))
                .isInstanceOf(IncorrectArgumentException.class);

        assertThatThrownBy(() -> usersService.getOrCreateUser("", "name"))
                .isInstanceOf(IncorrectArgumentException.class);

        assertThatThrownBy(() -> usersService.getOrCreateUser("  ", "name"))
                .isInstanceOf(IncorrectArgumentException.class);
    }

    @Test
    void getOrCreateUser_EmailExists_ReturnsUser() {
        User existing = getUser();
        User user = usersService.getOrCreateUser(existing.getEmail(), "name");

        assertThat(user.getId()).isEqualTo(existing.getId());
        assertThat(user.getEmail()).isEqualTo(existing.getEmail());
        assertThat(user.getName()).isEqualTo(existing.getName());
        assertThat(user.getIsValidated()).isEqualTo(existing.getIsValidated());
    }

    @Test
    void getOrCreateUser_NewEmail_CreatesUser() {
        // With name
        User user = usersService.getOrCreateUser("getOrCreateUser_NewEmail_0", "getOrCreateUser_NewEmail_1");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("getOrCreateUser_NewEmail_0");
        assertThat(user.getName()).isEqualTo("getOrCreateUser_NewEmail_1");
        assertThat(user.getWeekStart()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(user.getIsValidated()).isEqualTo(false);

        // Without name
        user = usersService.getOrCreateUser("getOrCreateUser_NewEmail_2", null);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("getOrCreateUser_NewEmail_2");
        assertThat(user.getName()).isNull();
        assertThat(user.getWeekStart()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(user.getIsValidated()).isEqualTo(false);
    }
}
