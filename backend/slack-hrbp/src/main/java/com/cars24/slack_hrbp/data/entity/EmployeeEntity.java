package com.cars24.slack_hrbp.data.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("Employee")
public class EmployeeEntity {

    @Id
    private String id; // Use String for elementId()

    @NotNull
    @Property("userId")
    private String userId;

    @NotBlank
    @Length(max = 20, min = 5, message = "Length should be 5-20")
    private String username;

    @NotBlank
    @Email
    @Length(max = 30, min = 5, message = "Length should be 5-30")
    @Property("email")
    private String email;

    private String encryptedPassword;

    private String managerName;
    private String managerId;

    @Property("roles")
    private List<String> roles = new ArrayList<>();

    // Relationship with Manager
    @Relationship(type = "REPORTED_BY", direction = Relationship.Direction.OUTGOING)
    private EmployeeEntity manager;


}
