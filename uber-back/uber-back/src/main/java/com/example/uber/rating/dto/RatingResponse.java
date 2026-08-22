package com.example.uber.rating.dto;

import com.example.uber.rating.Rating;
import java.time.LocalDateTime;

public record RatingResponse(
        Integer id,
        Integer rideId,
        Short driverRating,
        Short vehicleRating,
        String comment,
        LocalDateTime createdAt) {

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getRide().getId(),
                rating.getDriverRating(),
                rating.getVehicleRating(),
                rating.getComment(),
                rating.getCreatedAt());
    }
}
