package com.github.kshashov.timetracker.data.service.admin.user;

import com.github.kshashov.timetracker.data.entity.user.User;

public interface UsersService {
    User getOrCreateUser(String email, String name);

    boolean validate(User user);
}
