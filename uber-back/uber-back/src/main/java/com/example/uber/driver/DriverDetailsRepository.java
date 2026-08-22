package com.example.uber.driver;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverDetailsRepository extends JpaRepository<DriverDetails, Integer> {

    Optional<DriverDetails> findByUserId(Integer userId);
}
