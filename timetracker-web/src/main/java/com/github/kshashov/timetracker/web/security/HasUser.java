package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;

public interface HasUser {

    default User getUser() {
        return SecurityUtils.getCurrentUser().getUser();
    }
}
