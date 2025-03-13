package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.response.GraphResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class GraphDaoImpl {

    private final AttendanceRepository attendanceRepository;
    public GraphResponse getGraph(@RequestParam String userid , String month){

        List<AttendanceEntity> bis = attendanceRepository.findByUserid(userid);
        log.info("{}", bis);

        List<AttendanceEntity> lis = attendanceRepository.findByUseridAndDateStartingWith(userid,month);
        HashMap<String,Integer> typecount = new HashMap<>();

         for(AttendanceEntity ent : lis){
             log.info("{}" , ent.getType());
             typecount.put(ent.getType(), typecount.getOrDefault(ent.getType() , 0) + 1);
         }

         log.info("Type Count Map: {}", typecount);

         GraphResponse graphResponse = new GraphResponse();
         graphResponse.setTypeCounts(typecount);

         return graphResponse;
    }
}
