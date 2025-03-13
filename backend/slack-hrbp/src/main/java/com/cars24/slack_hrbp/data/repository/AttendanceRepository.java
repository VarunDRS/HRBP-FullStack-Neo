package com.cars24.slack_hrbp.data.repository;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    List<AttendanceEntity> findByDateStartingWith(String monthYear);
    List<AttendanceEntity> findByUseridAndDateStartingWith(String userid, String month);
    List<AttendanceEntity> findByUserid(String userid);
}