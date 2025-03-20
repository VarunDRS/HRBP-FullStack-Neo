package com.cars24.slack_hrbp.data.repository;

import com.cars24.slack_hrbp.data.entity.ProfileEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProfileRepository extends MongoRepository<ProfileEntity, String> {
    boolean existsByEmail(@NotNull(
            message = "Email cannot be null"
    ) @Email(
            message = "Please provide a valid email address"
    ) String email);

    ProfileEntity findByEmail(@NotNull(
            message = "Email cannot be null"
    ) @Email(
            message = "Please provide a valid email address"
    ) String email);
}