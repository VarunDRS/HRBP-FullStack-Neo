package com.cars24.slack_hrbp.data.repository;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    List<AttendanceEntity> findByDateStartingWith(String monthYear);
    List<AttendanceEntity> findByUseridAndDateStartingWith(String userid, String month);
    List<AttendanceEntity> findByUserid(String userid);

    @Query("{'date': { $regex: ?0, $options: 'i' }}")
    Page<AttendanceEntity> findByDateStartingWith(String monthYear, Pageable pageable);

    // Fetch distinct usernames (MongoDB way)
    @Query(value = "{ 'date': { $regex: ?0 } }", fields = "{ 'username': 1 }")
    Page<AttendanceEntity> findDistinctByDateStartingWith(String monthYear, Pageable pageable);

    // Fetch attendance records for a list of usernames
    List<AttendanceEntity> findByDateStartingWithAndUsernameIn(String monthYear, List<String> usernames);

    // Fetch distinct usernames (MongoDB way)
    @Query(value = "{ 'date': { $regex: ?0 } }", fields = "{ 'username': 1 }")
    List<AttendanceEntity> findDistinctByDateStartingWith(String monthYear);

    // Fetch usernames only (better performance)
    @Aggregation(pipeline = {
            "{ $match: { 'date': { $regex: ?0 } } }",
            "{ $group: { _id: '$username' } }",
            "{ $sort: { '_id': 1 } }"
    })
    List<String> findDistinctUsernamesByMonth(String monthYear);

    //trying for manager
    // Fetch distinct usernames for employees under a manager
    @Aggregation(pipeline = {
            "{ $match: { 'date': { $regex: ?0 }, 'managerId': ?1 } }",
            "{ $group: { _id: '$username' } }",
            "{ $sort: { '_id': 1 } }"
    })
    List<String> findDistinctUsernamesByManager(String monthYear, String managerId);

    // Fetch attendance records for employees under a specific manager



}