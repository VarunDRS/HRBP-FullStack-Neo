package com.cars24.slack_hrbp.service;


import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HrService {

    String updateUser(EmployeeUpdateRequest employeeUpdateRequest);

    Page<List<String>> getAllUsers(String userId, int page, int limit,String searchtag);

    public long getTotalEmployeesCount(String searchtag);

    public String deleteEntry(String userId,String date);

}
