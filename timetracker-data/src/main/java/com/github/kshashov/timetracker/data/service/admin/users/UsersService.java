package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface UsersService {
    User updateUser(@NotNull User user, @NotNull User updatedUser);

    User updateUser(@NotNull User user);

    User getOrCreateUser(String email, String name);

    boolean validate(@NotNull User user);
}
