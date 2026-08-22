package com.example.uber.ride;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Integer> {

    List<Ride> findByDriverIdAndStatus(Integer driverId, RideStatus status);

    Optional<Ride> findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(Integer driverId, RideStatus status);
}
