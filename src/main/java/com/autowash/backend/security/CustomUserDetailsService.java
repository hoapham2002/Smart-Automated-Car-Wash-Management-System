package com.autowash.backend.security;

import com.autowash.backend.entity.User;
import com.autowash.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Authenticates by phone number (this app's "username"). Returns the User
 * entity itself as the principal - User implements UserDetails directly,
 * so no separate wrapper class is needed (see SecurityUtils.currentUser()).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return userRepository.findByPhone(phone)
                .filter(User::isActive)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với số điện thoại: " + phone));
    }
}
