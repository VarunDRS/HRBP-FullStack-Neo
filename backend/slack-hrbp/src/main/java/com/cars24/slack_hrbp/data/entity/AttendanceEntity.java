package com.cars24.slack_hrbp.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Attendance")
@Data
public class AttendanceEntity {

    @Id
    private String id;
    private String userid;

    private String username;

    private String date;
    private String reason;

    private String type;

}
