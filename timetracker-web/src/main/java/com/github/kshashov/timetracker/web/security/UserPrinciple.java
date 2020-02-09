package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;

public interface UserPrinciple {
    User getUser();
}
