package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.service.admin.user.UsersService;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseUserTest extends BaseTest {
    User user;

    @Autowired
    UsersService usersService;

    @BeforeAll
    void prepareUser() {
        user = usersService.getOrCreateUser("test@mail.com", "test");
        assertThat(user).isNotNull();
    }
}
