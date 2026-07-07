package com.autowash.backend.security;

import com.autowash.backend.entity.User;
import com.autowash.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extracts "Authorization: Bearer <token>", validates it, and populates the
 * SecurityContext with the User principal - every 🔒 endpoint in the API doc
 * depends on this filter running before Spring Security's authorization checks.
 *
 * Loads the user directly via UserRepository (by the id embedded in the JWT
 * subject claim) rather than routing through CustomUserDetailsService (which
 * is phone-based, for the login flow) - avoids an unnecessary extra query.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtTokenProvider.validateToken(token)) {
            try {
                UUID userId = jwtTokenProvider.getUserId(token);
                userRepository.findById(userId)
                        .filter(User::isActive)
                        .ifPresent(user -> authenticate(user, request));
            } catch (Exception ex) {
                log.debug("Could not set authentication from JWT: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user, HttpServletRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
