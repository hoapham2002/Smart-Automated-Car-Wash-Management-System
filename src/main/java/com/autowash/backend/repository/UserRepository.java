package com.autowash.backend.repository;

import com.autowash.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** A01/A02 - uniqueness check on register, and login lookup. */
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    /**
     * C03 (Giai đoạn 2) - customers whose date_of_birth falls on today's
     * month/day, regardless of birth year. Used by BirthdayBonusJob.
     * Native SQL since JPQL has no portable EXTRACT()/date-part function
     * across Hibernate versions without a custom function registration.
     */
    @Query(value = """
            SELECT * FROM users
            WHERE role = 'customer'
              AND is_active = true
              AND date_of_birth IS NOT NULL
              AND EXTRACT(MONTH FROM date_of_birth) = :month
              AND EXTRACT(DAY FROM date_of_birth) = :day
            """, nativeQuery = true)
    List<User> findCustomersWithBirthdayOn(@Param("month") int month, @Param("day") int day);
}
