package com.autowash.backend.config;

import com.autowash.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security. Coarse-grained rules are declared here at the URL
 * level (matching the MAJORITY case per role for each path prefix); the
 * handful of endpoints whose role doesn't match their prefix's default
 * (e.g. GET /admin/bookings being Staff+Admin while the rest of /admin/**
 * is Admin-only, or POST /wash/sessions/:id/rating being Customer-only
 * while the rest of /wash/** is Staff+Admin) are overridden with
 * @PreAuthorize directly on those controller methods - see WashController
 * and AdminDashboardController. @EnableMethodSecurity below is what makes
 * those annotations take effect.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC_GET = {
            "/services/**",
            "/slots/**",
            "/actuator/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_POST = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/research/survey"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                        // Admin-only by default; specific Staff+Admin exceptions (e.g.
                        // GET /admin/bookings) are carved out with @PreAuthorize on the
                        // controller method itself, which takes precedence.
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Staff+Admin by default; the Customer-only rating endpoint is
                        // carved out with @PreAuthorize on WashController.rate().
                        .requestMatchers("/wash/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings/walkin").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/payments/invoices/*/pay").hasAnyRole("STAFF", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
