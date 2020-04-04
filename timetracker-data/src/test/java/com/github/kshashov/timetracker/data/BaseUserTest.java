package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseUserTest extends BaseTest {
    User user;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeAll
    void prepareUser() {
        user = usersRepository.findById(1L).get();
    }
}
