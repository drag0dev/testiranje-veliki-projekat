package com.example.uber.rating;

import com.example.uber.common.exception.ConflictException;
import com.example.uber.common.exception.ForbiddenException;
import com.example.uber.common.exception.NotFoundException;
import com.example.uber.rating.dto.RatingRequest;
import com.example.uber.ride.Ride;
import com.example.uber.ride.RideRepository;
import com.example.uber.ride.RideStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private static final int RATING_WINDOW_DAYS = 3;

    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public Rating submitRating(Integer rideId, String passengerEmail, RatingRequest request) {
        Ride ride = getRideOrThrow(rideId);

        if (!ride.getPassenger().getEmail().equalsIgnoreCase(passengerEmail)) {
            throw new ForbiddenException("Only the passenger of ride " + rideId + " can rate it");
        }

        if (ride.getStatus() != RideStatus.FINISHED || ride.getEndTime() == null) {
            throw new ConflictException("Ride " + rideId + " has not finished yet");
        }

        if (ride.getEndTime().isBefore(LocalDateTime.now().minusDays(RATING_WINDOW_DAYS))) {
            throw new ConflictException(
                    "The " + RATING_WINDOW_DAYS + "-day rating window for ride " + rideId + " has expired");
        }

        if (ratingRepository.existsByRideId(rideId)) {
            throw new ConflictException("Ride " + rideId + " has already been rated");
        }

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setPassenger(ride.getPassenger());
        rating.setDriverRating(request.driverRating().shortValue());
        rating.setVehicleRating(request.vehicleRating().shortValue());
        rating.setComment(request.comment());
        rating.setCreatedAt(LocalDateTime.now());

        return ratingRepository.save(rating);
    }

    private Ride getRideOrThrow(Integer rideId) {
        return rideRepository
                .findById(rideId)
                .orElseThrow(() -> new NotFoundException("Ride " + rideId + " not found"));
    }
}
