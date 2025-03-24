package com.cars24.slack_hrbp.data.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
@Valid

public class AddLeaveRequest {

    private String date;
    private String leaveType;
    private String reason;

}
