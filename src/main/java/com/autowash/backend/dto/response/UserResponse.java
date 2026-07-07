package com.autowash.backend.dto.response;

import com.autowash.backend.entity.User;
import com.autowash.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Full profile shape for GET /me (not in A01-A06's scope, but the DTO is
 * created now so the entity/response layer is ready when that endpoint is
 * implemented - see GIAIDOAN1_TUAN1_README.md for the gap this fills).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private UserRole role;
    private String occupation;
    private String gender;
    private LocalDate dateOfBirth;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .occupation(user.getOccupation())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }
}
