package com.cars24.slack_hrbp.data.request;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Valid

public class CreateEmployeeRequest {

    @Column(name = "userId")
    @NotNull(message = "UserId cannot be null")
    @NotBlank(message = "UserId cannot be empty")
    private String userId;

    @Column(name = "username")
    @NotNull(message = "Username cannot be null")
    @Length(min = 3, max = 15, message = "Length of the name should be greater than 2 characters and less than 15 characters")
    private String username;

    @NotNull(message = "Password cannot be null")
    @Length(min = 8, message = "Password should be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)")
    private String password;


    @Column(name = "email")
    @NotNull(message = "Email cannot be null")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(name = "roles")
    @NotNull(message = "Role cannot be empty")
    private List<String> roles;

    private String encryptedPassword;

    @Column(name = "managerName")
    private String managerName;

    @Column(name = "managerId")
    private String managerId;
}
