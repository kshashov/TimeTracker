package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.data.entity.user.User;

import javax.validation.constraints.NotNull;

public interface AuthorizedUsersService {
    User updateUser(@NotNull User user, @NotNull Long userId, @NotNull UserInfo userInfo);

    boolean validate(@NotNull User user, @NotNull Long userId, @NotNull String name);
}
