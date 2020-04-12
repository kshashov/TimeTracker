package com.github.kshashov.timetracker.data.service.admin.user;

import com.github.kshashov.timetracker.core.errors.IncorrectArgumentException;
import com.github.kshashov.timetracker.data.BaseUserTest;
import com.github.kshashov.timetracker.data.entity.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        assertThat(user.getIsValidated()).isEqualTo(false);

        // Without name
        user = usersService.getOrCreateUser("getOrCreateUser_NewEmail_2", null);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("getOrCreateUser_NewEmail_2");
        assertThat(user.getName()).isNull();
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
        User user = usersService.getOrCreateUser("updateUser_IncorrectUser_0", "updateUser_IncorrectUser_1");

        // Incorrect email
        user.setEmail(null);
        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(IncorrectArgumentException.class);

        user.setEmail("");
        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(IncorrectArgumentException.class);

        user.setEmail("  ");
        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(IncorrectArgumentException.class);

        user.setEmail("updateUser_IncorrectUser_0");

        // Incorrect name

        user.setName(null);
        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(IncorrectArgumentException.class);

        user.setName("");
        assertThatThrownBy(() -> usersService.updateUser(user))
                .isInstanceOf(IncorrectArgumentException.class);

        user.setName("");
        assertThatThrownBy(() -> usersService.updateUser(user))
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
        // TODO
    }

    @Test
    void updateUser_CorrectUser_Ok() {
        // TODO
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
