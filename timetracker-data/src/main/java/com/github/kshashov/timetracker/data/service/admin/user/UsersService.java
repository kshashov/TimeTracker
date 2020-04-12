package com.github.kshashov.timetracker.data.service.admin.user;

import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface UsersService {
    User getOrCreateUser(String email, String name);

    User updateUser(@NotNull User user);

    User updateUser(@NotNull User user, @NotNull User updatedUser);

    boolean validate(@NotNull User user);
}
