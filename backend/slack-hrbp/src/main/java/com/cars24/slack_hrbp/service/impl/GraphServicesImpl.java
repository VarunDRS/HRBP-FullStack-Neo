package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.GraphDaoImpl;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.response.GraphResponse;
import com.cars24.slack_hrbp.service.GraphServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor

public class GraphServicesImpl implements GraphServices {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private final GraphDaoImpl graphDao;

    public GraphResponse getGraph(@RequestParam String userid , String month){
            return graphDao.getGraph(userid,month);
    }
}
