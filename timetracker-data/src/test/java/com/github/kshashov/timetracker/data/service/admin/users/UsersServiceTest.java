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
public class UsersServiceTest extends BaseUserTest {

    @Autowired
    UsersService usersService;
    @Autowired
    UsersAdminService usersAdminService;

    //
    // VALIDATE
    //

    @Test
    void validate_IncorrectUser_ReturnsFalse() {
        User user = usersAdminService.getOrCreateUser("updateUser_IncorrectUser_0", "updateUser_IncorrectUser_1`");
        assertThat(user.getIsValidated()).isFalse();

        user.setName("");
        assertThat(usersService.validate(user.getId(), "")).isFalse();
        assertThat(user.getIsValidated()).isFalse();

        user.setName("  ");
        assertThat(usersService.validate(user.getId(), "  ")).isFalse();
        assertThat(user.getIsValidated()).isFalse();
    }

    @Test
    void validate_Ok() {
        User user = usersAdminService.getOrCreateUser("validate_Ok_0", "validate_Ok_1");

        assertThat(user.getIsValidated()).isFalse();
        assertThat(usersService.validate(user.getId(), "validate_Ok_1")).isTrue();
        assertThat(user.getIsValidated()).isTrue();
    }

    //
    // UPDATE
    //

    @Test
    void updateUser_IncorrectUser_IncorrectArgumentException() {
        // Incorrect name
        User user = usersAdminService.getOrCreateUser("updateUser_IncorrectUser_0", "updateUser_IncorrectUser_1");

        UserInfo userInfo = new UserInfo();
        userInfo.setWeekStart(DayOfWeek.FRIDAY);

        userInfo.setName(null);
        assertThatThrownBy(() -> usersService.updateUser(user.getId(), userInfo))
                .isInstanceOf(IncorrectArgumentException.class);

        userInfo.setName("");
        assertThatThrownBy(() -> usersService.updateUser(user.getId(), userInfo))
                .isInstanceOf(IncorrectArgumentException.class);

        userInfo.setName("  ");
        assertThatThrownBy(() -> usersService.updateUser(user.getId(), userInfo))
                .isInstanceOf(IncorrectArgumentException.class);

        // Incorrect week start
        userInfo.setName("updateUser_IncorrectUser_2");
        userInfo.setWeekStart(null);
        assertThatThrownBy(() -> usersService.updateUser(user.getId(), userInfo))
                .isInstanceOf(IncorrectArgumentException.class);

    }

    @Test
    void updateUser_Ok() {
        User user = usersAdminService.getOrCreateUser("updateUser_Ok_0", "updateUser_Ok_1");

        UserInfo userInfo = new UserInfo();
        userInfo.setName("updateUser_Ok_2");
        userInfo.setWeekStart(DayOfWeek.FRIDAY);

        User result = usersService.updateUser(user.getId(), userInfo);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo("updateUser_Ok_0");
        assertThat(result.getIsValidated()).isFalse();
        assertThat(result.getName()).isEqualTo("updateUser_Ok_2");
        assertThat(result.getWeekStart()).isEqualTo(DayOfWeek.FRIDAY);
    }
}
