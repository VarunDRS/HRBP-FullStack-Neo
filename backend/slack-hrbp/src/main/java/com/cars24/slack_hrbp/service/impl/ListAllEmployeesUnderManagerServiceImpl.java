package com.cars24.slack_hrbp.service.impl;


import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ListAllEmployeesUnderManagerServiceImpl{

    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    public Page<List<String>> getAllEmployeesUnderManager(String userId, int page, int limit,String searchtag) {
        return listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(userId, page, limit,searchtag);
    }

}
