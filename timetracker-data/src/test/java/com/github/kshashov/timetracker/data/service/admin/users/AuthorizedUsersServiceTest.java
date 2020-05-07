package com.github.kshashov.timetracker.data.service.admin.users;

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
public class AuthorizedUsersServiceTest extends BaseUserTest {

    @Autowired
    AuthorizedUsersService usersService;
    @Autowired
    UsersAdminService usersAdminService;

    //
    // VALIDATE BY USER
    //

    @Test
    void validate_UserHasNoPermission_NoPermissionException() {

        // Other user
        User user0 = usersAdminService.getOrCreateUser("validate_UserHasNoPermission_0", "validate_UserHasNoPermission_1");
        User user1 = usersAdminService.getOrCreateUser("validate_UserHasNoPermission_2", "validate_UserHasNoPermission_3");

        assertThatThrownBy(() -> usersService.validate(user0, user1.getId(), "validate_UserHasNoPermission_4"))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    void validate_CorrectUser_Ok() {
        User user = usersAdminService.getOrCreateUser("validate_CorrectUser_0", "validate_CorrectUser_1");
        assertThat(user.getIsValidated()).isFalse();

        boolean result = usersService.validate(user, user.getId(), "validate_CorrectUser_2");
        user = getUsersRepository().getOne(user.getId());

        assertThat(result).isTrue();
        assertThat(user.getIsValidated()).isTrue();
        assertThat(user.getName()).isEqualTo("validate_CorrectUser_2");
    }

    //
    // UPDATE BY USER
    //

    @Test
    void updateUser_UserHasNoPermission_NoPermissionException() {
        // Other user
        User user0 = usersAdminService.getOrCreateUser("updateUser_UserHasNoPermission_0", "updateUser_UserHasNoPermission_1");
        User user1 = usersAdminService.getOrCreateUser("updateUser_UserHasNoPermission_2", "updateUser_UserHasNoPermission_3");

        UserInfo userInfo = new UserInfo();
        userInfo.setName("updateUser_UserHasNoPermission_4");
        userInfo.setWeekStart(DayOfWeek.FRIDAY);

        assertThatThrownBy(() -> usersService.updateUser(user1, user0.getId(), userInfo))
                .isInstanceOf(NoPermissionException.class);
    }

    @Test
    void updateUser_CorrectUser_Ok() {
        // Same user
        User user = usersAdminService.getOrCreateUser("updateUser_CorrectUser_0", "updateUser_CorrectUser_1");
        UserInfo userInfo = new UserInfo();
        userInfo.setName("updateUser_CorrectUser_2");
        userInfo.setWeekStart(DayOfWeek.FRIDAY);

        // Same user
        User result = usersService.updateUser(user, user.getId(), userInfo);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getIsValidated()).isEqualTo(user.getIsValidated());
        assertThat(result.getName()).isEqualTo(userInfo.getName());
        assertThat(result.getWeekStart()).isEqualTo(userInfo.getWeekStart());
    }
}
