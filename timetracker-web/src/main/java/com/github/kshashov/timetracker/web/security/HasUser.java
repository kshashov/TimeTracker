package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

public interface HasUser {

    default User getUser() {
        return SecurityUtils.getCurrentUser()
                .map(UserPrincipal::getUser)
                .orElseThrow(CurrentUserNotFoundException::new);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    class CurrentUserNotFoundException extends RuntimeException {
    }
}
