package com.autowash.autowash_pro.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.autowash.autowash_pro.entity.Customer;
import com.autowash.autowash_pro.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String phone)
            throws UsernameNotFoundException {
        Customer customer = customerRepository
            .findByPhone(phone)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy user: " + phone));

        return User.builder()
            .username(customer.getPhone())
            .password(customer.getPassword())
            .roles(customer.isAdmin() ? "ADMIN" : "CUSTOMER")
            .build();
    }
}