package com.cars24.slack_hrbp.data.request;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Valid

public class CreateEmployeeRequest {

    @NotNull(message = "Email cannot be null")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Role cannot be empty")
    private List<String> roles;

    @Column(name = "managerEmail")
    @Email(message = "Please provide a valid email address")
    private String managerEmail;
}
