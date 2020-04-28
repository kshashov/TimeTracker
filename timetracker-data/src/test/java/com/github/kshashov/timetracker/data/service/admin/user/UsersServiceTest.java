package com.github.kshashov.timetracker.data.service.admin.user;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.core.errors.NoPermissionException;
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

    //
    // VALIDATE
    //

    @Test
    void validate_UserIdIsNull_NullPointerException() {
        User user = correctUser("validate_UserIdIsNull");
        user.setId(null);

        assertThatThrownBy(() -> usersService.validate(user))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void validate_IncorrectUser_ReturnsFalse() {
        User user = usersService.getOrCreateUser("updateUser_IncorrectUser_0", "updateUser_IncorrectUser_1`");
        assertThat(user.getIsValidated()).isFalse();

        user.setName(null);
        assertThat(usersService.validate(user)).isFalse();
        assertThat(user.getIsValidated()).isFalse();

        user.setName("");
        assertThat(usersService.validate(user)).isFalse();
        assertThat(user.getIsValidated()).isFalse();

        user.setName("  ");
        assertThat(usersService.validate(user)).isFalse();
        assertThat(user.getIsValidated()).isFalse();
    }

    @Test
    void validate_Ok() {
        User user = usersService.getOrCreateUser("validate_Ok_0", "validate_Ok_1");

        assertThat(user.getIsValidated()).isFalse();
        assertThat(usersService.validate(user)).isTrue();
        assertThat(user.getIsValidated()).isTrue();
    }

    //
    // UPDATE
    //

    @Test
    void updateUser_UserIdIsNull_NullPointerException() {
        User user = correctUser("updateUser_UserIdIsNull");
        user.setId(null);

        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateUser_IncorrectUser_IncorrectArgumentException() {
        // Incorrect email
        User user0 = usersService.getOrCreateUser("updateUser_IncorrectUser_0", "updateUser_IncorrectUser_1");

        user0.setEmail(null);
        assertThatThrownBy(() -> usersService.updateUser(user0))
                .isInstanceOf(IncorrectArgumentException.class);

        user0.setEmail("");
        assertThatThrownBy(() -> usersService.updateUser(user0))
                .isInstanceOf(IncorrectArgumentException.class);

        user0.setEmail("  ");
        assertThatThrownBy(() -> usersService.updateUser(user0))
                .isInstanceOf(IncorrectArgumentException.class);

        user0.setEmail("updateUser_IncorrectUser_0");

        // Incorrect name
        User user1 = usersService.getOrCreateUser("updateUser_IncorrectUser_1", "updateUser_IncorrectUser_2");

        user1.setName(null);
        assertThatThrownBy(() -> usersService.updateUser(user1))
                .isInstanceOf(IncorrectArgumentException.class);

        user1.setName("");
        assertThatThrownBy(() -> usersService.updateUser(user1))
                .isInstanceOf(IncorrectArgumentException.class);

        user1.setName("");
        assertThatThrownBy(() -> usersService.updateUser(user1))
                .isInstanceOf(IncorrectArgumentException.class);

        // Incorrect week start
        User user2 = usersService.getOrCreateUser("updateUser_IncorrectUser_2", "updateUser_IncorrectUser_3");

        user2.setWeekStart(null);
        assertThatThrownBy(() -> usersService.updateUser(user2))
                .isInstanceOf(IncorrectArgumentException.class);

    }

    @Test
    void updateUser_Ok() {
        User user = usersService.getOrCreateUser("updateUser_Ok_0", "updateUser_Ok_1");
        user.setEmail("updateUser_Ok_2");
        user.setName("updateUser_Ok_3");

        User result = usersService.updateUser(user);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo("updateUser_Ok_2");
        assertThat(result.getName()).isEqualTo("updateUser_Ok_3");
        assertThat(result.getIsValidated()).isFalse();
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateUser_UserHasNoPermission_NoPermissionException() {
        // Other user
        User user0 = usersService.getOrCreateUser("updateUser_UserHasNoPermission_0", "updateUser_UserHasNoPermission_1");
        User user1 = usersService.getOrCreateUser("updateUser_UserHasNoPermission_2", "updateUser_UserHasNoPermission_3");

        assertThatThrownBy(() -> usersService.updateUser(user1, user0))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    void updateUser_CorrectUser_Ok() {
        // Same user
        User user = usersService.getOrCreateUser("updateUser_UserHasNoPermission_0", "updateUser_UserHasNoPermission_1");

        user.setName("updateUser_UserHasNoPermission_2");
        user.setWeekStart(DayOfWeek.FRIDAY);

        User result = usersService.updateUser(user, user);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getIsValidated()).isEqualTo(user.getIsValidated());
        assertThat(result.getWeekStart()).isEqualTo(user.getWeekStart());
    }

    static User correctUser(String email) {
        return correctUser(email, "name");
    }

    static User correctUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}
