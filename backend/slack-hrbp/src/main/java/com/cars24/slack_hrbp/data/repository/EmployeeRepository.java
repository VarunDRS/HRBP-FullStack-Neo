package com.cars24.slack_hrbp.data.repository;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.response.UpdateEmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends Neo4jRepository<EmployeeEntity, String>, PagingAndSortingRepository<EmployeeEntity, String> {

    @Query("""
    CREATE (e:Employee {userId: $userId, username: $username, email: $email,
        encryptedPassword: $encryptedPassword,
        managerName: $managerName, managerId: $managerId, roles: $roles}) 
    WITH e
    OPTIONAL MATCH (m:Employee {userId: $managerId}) 
    FOREACH (_ IN CASE WHEN m IS NOT NULL THEN [1] ELSE [] END | CREATE (e)-[:REPORTED_BY]->(m))
    RETURN e
""")
    EmployeeEntity createEmployeeWithManager(
            @Param("userId") String userId,
            @Param("username") String username,
            @Param("email") String email,
            @Param("encryptedPassword") String encryptedPassword,
            @Param("managerName") String managerName,
            @Param("managerId") String managerId,
            @Param("roles") List<String> roles
    );


    @Query("MATCH (e:Employee) WHERE e.email = $email RETURN COUNT(e) > 0")
    boolean existsByEmail(@Param("email") String email);

    @Query("MATCH (e:Employee) WHERE e.email = $email RETURN e")
    EmployeeEntity findByEmail(@Param("email") String email);

    @Query("MATCH (e:Employee {userId: $userId}) " +
            "SET e.roles = $roles " +
            "RETURN e")
    EmployeeEntity updateEmployeeRoles(
            @Param("userId") String userId,
            @Param("roles") List<String> roles
    );



    @Query("MATCH (e:Employee {userId: $userId}) RETURN e")
    Optional<EmployeeEntity> findByUserId(@Param("userId") String userId);

    @Query("MATCH (e:Employee {userId: $userId}) RETURN COUNT(e) > 0")
    boolean existsByUserId(@Param("userId") String userId);

    @Query("MATCH (e:Employee {userId: $userId}) SET e.encryptedPassword = $encryptedPassword RETURN e")
    EmployeeEntity updatePassword(@Param("userId") String userId, @Param("encryptedPassword") String encryptedPassword);


    @Query("MATCH (e:Employee {userId: $userId}) DETACH DELETE e")
    void deleteByUserId(@Param("userId") String userId);

    @Query(value = "MATCH (e:Employee)-[:REPORTED_BY]->(m:Employee {userId: $managerId}) RETURN e",
            countQuery = "MATCH (e:Employee)-[:REPORTED_BY]->(m:Employee {userId: $managerId}) RETURN COUNT(e)")
    Page<EmployeeEntity> findByManagerId(@Param("managerId") String managerId, Pageable pageable);

    @Query(value = "MATCH (e:Employee) RETURN e",
            countQuery = "MATCH (e:Employee) RETURN COUNT(e)")
    Page<EmployeeEntity> findAll(Pageable pageable);

    @Query("MATCH (e:Employee)-[:REPORTED_BY]->(m:Employee {userId: $managerId}) RETURN COUNT(e) > 0")
    boolean existsByManagerId(@Param("managerId") String managerId);

    @Query("MATCH (:Employee {userId: $managerId})<-[:REPORTED_BY*]-(e:Employee) RETURN COUNT(e)")
    long countByManagerId(@Param("managerId") String managerId);

    @Query("MATCH (:Employee {userId: $managerId})<-[:REPORTED_BY*]-(e:Employee) " +
            "WHERE TOLOWER(e.username) STARTS WITH TOLOWER($searchtag) " +
            "   OR TOLOWER(e.username) CONTAINS TOLOWER($searchtag) " +
            "RETURN COUNT(e)")
    long countByManagerIdAndSearchtag(@Param("managerId") String managerId,
                                      @Param("searchtag") String searchtag);

    @Query("MATCH (e:Employee) RETURN COUNT(e)")
    long count();

    @Query("MATCH (e:Employee) " +
            "WHERE $searchtag <> '' AND (TOLOWER(e.username) STARTS WITH TOLOWER($searchtag) " +
            "   OR TOLOWER(e.username) CONTAINS TOLOWER($searchtag)) " +
            "RETURN COUNT(e)")
    long countBySearchtag(@Param("searchtag") String searchtag);

    @Query(value = "MATCH (e:Employee)-[:REPORTED_BY]->(m:Employee {userId: $managerId}) RETURN e",
            countQuery = "MATCH (e:Employee)-[:REPORTED_BY]->(m:Employee {userId: $managerId}) RETURN COUNT(e)")
    List<EmployeeEntity> findByManagerId(@Param("managerId") String managerId);


}
