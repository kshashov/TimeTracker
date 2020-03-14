package com.github.kshashov.timetracker.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static UserPrinciple getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        return (UserPrinciple) authentication.getPrincipal();
    }
}
