package com.cars24.slack_hrbp.controller;


import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/manager")
@Slf4j
@RequiredArgsConstructor

public class ManagerController {

    private final MonthBasedServiceImpl monthBasedService;
    private final UserServiceImpl userService;
    private final UseridAndMonthImpl useridandmonth;
    private final ListAllEmployeesUnderManagerServiceImpl listAllEmployeesUnderManager;
    private final EmployeeServiceImpl employeeService;
    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;
    private final HrServiceImpl hrService;

    @PreAuthorize("hasrole('MANAGER')")
    @GetMapping("bymonth")
    public ResponseEntity<Map<String, Map<String, String>>> getByMonth(@RequestParam String monthYear) {
        try {
            Map<String, Map<String, String>> reportData = monthBasedService.generateAttendanceReport(monthYear);
            return ResponseEntity.ok().body(reportData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){

        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userid,month);
        return resp;

    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userId);
        return resp;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/getAllEmployees/{userId}")
    public Page<List<String>> getAllEmployeesUnderManager(@PathVariable String userId){
        Page<List<String>> lis = listAllEmployeesUnderManager.getAllEmployeesUnderManager(userId,  0, 0);
        return lis;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/{userId}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        // Convert page number to zero-based index
        if (page > 0) page -= 1;

        Page<List<String>> users = hrService.getAllUsers(userId, page, limit);
        List<GetUserResponse> responses = new ArrayList<>();

        // Iterate over users.getContent()
        for (List<String> userDto : users.getContent()) {
            if (userDto.size() >= 3) { // Ensure list contains required values
                GetUserResponse res = new GetUserResponse();
                res.setUserId(userDto.get(0));
                res.setEmail(userDto.get(1));
                res.setUsername(userDto.get(2));
                responses.add(res);
            }
        }

        return ResponseEntity.ok().body(responses);
    }



    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/count/{userId}")
    public ResponseEntity<Map<String, Object>> getTotalUserCount(@PathVariable String userId,
                                                                 @RequestParam(value = "limit", defaultValue = "2") int limit) {

        long totalEmployees = listAllEmployeesUnderManagerDao.getTotalEmployeesCount(userId);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }


}
