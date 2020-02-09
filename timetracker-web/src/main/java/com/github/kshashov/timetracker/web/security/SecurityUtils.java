package com.github.kshashov.timetracker.web.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static UserPrinciple getCurrentUser() {
        return (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
