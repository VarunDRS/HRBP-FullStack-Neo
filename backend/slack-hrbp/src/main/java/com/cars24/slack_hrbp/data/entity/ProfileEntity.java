package com.cars24.slack_hrbp.data.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "profiles")
@Data

public class ProfileEntity {
    private String slackid;
    private String name;
    private String email;
    private String phone;

}
