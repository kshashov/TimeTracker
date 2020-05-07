package com.github.kshashov.timetracker.data.service.admin.users;

import com.github.kshashov.timetracker.data.entity.user.User;

public interface UsersAdminService {
    User getOrCreateUser(String email, String name);
}
