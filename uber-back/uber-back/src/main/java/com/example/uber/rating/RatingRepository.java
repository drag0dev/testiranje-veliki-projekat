package com.example.uber.rating;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    Optional<Rating> findByRideId(Integer rideId);

    boolean existsByRideId(Integer rideId);
}
