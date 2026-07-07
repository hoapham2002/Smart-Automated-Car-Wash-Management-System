package com.autowash.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** A01 - POST /auth/register */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "phone không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "phone không hợp lệ (vd: 0901000025)")
    private String phone;

    @Email(message = "email không hợp lệ")
    private String email;

    @NotBlank(message = "full_name không được để trống")
    private String fullName;

    @NotBlank(message = "password không được để trống")
    @Size(min = 8, message = "password phải có ít nhất 8 ký tự")
    private String password;

    private LocalDate dateOfBirth;
    private String gender;
    private String occupation;
    private String acquisitionChannel;
}
