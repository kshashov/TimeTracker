package com.github.kshashov.timetracker.web.security;

import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Stream;

public class SecurityUtils {

    public static boolean isUserLoggedIn() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            return SecurityUtils.isUserLoggedIn(context.getAuthentication());
        }
        return false;
    }

    private static boolean isUserLoggedIn(Authentication authentication) {
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() != null
                && authentication.getPrincipal() instanceof UserPrincipal;
    }

    public static Optional<UserPrincipal> getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication auth = context.getAuthentication();
            if (auth != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof UserPrincipal) {
                    return Optional.of((UserPrincipal) context.getAuthentication().getPrincipal());
                }
            }
        }
        // Anonymous or no authentication.
        return Optional.empty();
    }

    public static boolean hasValidatedUser() {
        try {
            return getCurrentUser()
                    .map(p -> (p.getUser() != null) && p.getUser().getIsValidated())
                    .orElse(false);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Tests if the request is an internal framework request. The test consists of
     * checking if the request parameter is present and if its value is consistent
     * with any of the request types know.
     *
     * @param request {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(ServletHelper.RequestType.values())
                .anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }
}
