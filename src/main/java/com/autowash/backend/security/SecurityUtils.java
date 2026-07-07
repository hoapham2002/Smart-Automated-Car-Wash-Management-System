package com.autowash.backend.security;

import com.autowash.backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Small helper around Spring Security's context, so services don't each
 * re-implement "who is calling me right now".
 *
 * Assumes CustomUserDetailsService (Week 1) authenticates with a UserDetails
 * implementation whose principal ultimately exposes the domain User - adjust
 * the cast in currentUser() if your UserDetails wrapper differs.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        // If your UserDetails implementation wraps User (e.g. CustomUserDetails.getUser()),
        // adapt this branch accordingly, e.g.:
        // if (principal instanceof CustomUserDetails cud) return cud.getUser();
        throw new IllegalStateException(
                "Unexpected principal type: " + principal.getClass() + " - adapt SecurityUtils.currentUser()");
    }

    public static UUID currentUserId() {
        return currentUser().getId();
    }
}
