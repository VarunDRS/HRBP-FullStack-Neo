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

//    @PreAuthorize("hasrole('MANAGER')")
//    @GetMapping("bymonth")
//    public ResponseEntity<Map<String, Map<String, String>>> getByMonth(@RequestParam String monthYear) {
//        try {
//            Map<String, Map<String, String>> reportData = monthBasedService.generateAttendanceReport(monthYear);
//            return ResponseEntity.ok().body(reportData);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error generating report: " + e.getMessage());
//        }
//    }

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

//    @PreAuthorize("hasRole('MANAGER')")
//    @GetMapping("/getAllEmployees/{userId}")
//    public Page<List<String>> getAllEmployeesUnderManager(@PathVariable String userId){
//        Page<List<String>> lis = listAllEmployeesUnderManager.getAllEmployeesUnderManager(userId,  0, 0);
//        return lis;
//    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/{userId}/{searchtag}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        // Convert page number to zero-based index
        if (page > 0) page -= 1;

        Page<List<String>> users = listAllEmployeesUnderManager.getAllEmployeesUnderManager(userId, page, limit, searchtag);
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
    @GetMapping("/displayUsers/count/{userId}/{searchtag}")
    public ResponseEntity<Map<String, Object>> getTotalUserCount(@PathVariable String userId,
                                                                 @PathVariable String searchtag,
                                                                 @RequestParam(value = "limit", defaultValue = "2") int limit) {
        long totalEmployees = listAllEmployeesUnderManagerDao.getTotalEmployeesCount(userId,searchtag);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getByMonthandManagerid2(
            @RequestParam String monthYear,@RequestParam String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        // Convert to zero-based index for pagination
        if (page > 0) page -= 1;

        try {
            // Fetch all data first
            Map<String, Map<String, String>> allReportData = monthBasedService.generateAttendanceReport2(monthYear,userId,page,limit);
            System.out.println("In Manager controller from Service Layer : "+ allReportData);
            // Convert to a list for pagination
            List<Map.Entry<String, Map<String, String>>> allUsersList = new ArrayList<>(allReportData.entrySet());

            // Paginate based on employees, not individual records
            int totalRecords = allUsersList.size();
            int startIndex = page * limit;
            int endIndex = Math.min(startIndex + limit, totalRecords);

            // Extract paginated data
            Map<String, Map<String, String>> paginatedReportData = new LinkedHashMap<>();
            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, Map<String, String>> entry = allUsersList.get(i);
                paginatedReportData.put(entry.getKey(), entry.getValue());
            }

            // Calculate total pages based on employees
            int totalPages = (int) Math.ceil((double) totalRecords / limit);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("reportData", paginatedReportData);
            response.put("totalPages", totalPages);
            response.put("currentPage", page + 1);
            response.put("pageSize", limit);
            response.put("totalRecords", totalRecords);
            System.out.println("The Response in controller is : "+response);

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

}
