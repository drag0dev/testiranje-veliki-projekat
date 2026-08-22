package com.example.uber.ride.dto;

import com.example.uber.ride.Ride;
import com.example.uber.ride.RideStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Integer id,
        RideStatus status,
        String startAddress,
        String endAddress,
        BigDecimal price,
        LocalDateTime startTime,
        LocalDateTime endTime) {

    public static RideResponse from(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getStatus(),
                ride.getStartAddress(),
                ride.getEndAddress(),
                ride.getPrice(),
                ride.getStartTime(),
                ride.getEndTime());
    }
}
