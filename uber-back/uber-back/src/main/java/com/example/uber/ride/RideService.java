package com.example.uber.ride;

import com.example.uber.common.exception.ConflictException;
import com.example.uber.common.exception.ForbiddenException;
import com.example.uber.common.exception.NotFoundException;
import com.example.uber.driver.DriverDetails;
import com.example.uber.driver.DriverDetailsRepository;
import com.example.uber.driver.DriverStatus;
import com.example.uber.notification.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverDetailsRepository driverDetailsRepository;
    private final NotificationService notificationService;

    @Transactional
    public Ride finishRide(Integer rideId, String driverEmail) {
        Ride ride = getRideOrThrow(rideId);

        if (ride.getDriver() == null || !ride.getDriver().getEmail().equalsIgnoreCase(driverEmail)) {
            throw new ForbiddenException("Only the assigned driver can finish ride " + rideId);
        }

        if (ride.getStatus() != RideStatus.ACTIVE) {
            throw new ConflictException(
                    "Ride " + rideId + " cannot be finished from status " + ride.getStatus());
        }

        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        Ride finishedRide = rideRepository.save(ride);

        updateDriverAvailability(finishedRide.getDriver().getId());
        notificationService.notifyRideFinished(finishedRide);

        return finishedRide;
    }

    private Ride getRideOrThrow(Integer rideId) {
        return rideRepository
                .findById(rideId)
                .orElseThrow(() -> new NotFoundException("Ride " + rideId + " not found"));
    }

    private void updateDriverAvailability(Integer driverId) {
        boolean hasUpcomingRide =
                rideRepository
                        .findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(driverId, RideStatus.ACCEPTED)
                        .isPresent();

        if (hasUpcomingRide) {
            return;
        }

        driverDetailsRepository
                .findByUserId(driverId)
                .ifPresent(
                        details -> {
                            details.setStatus(DriverStatus.AVAILABLE);
                            driverDetailsRepository.save(details);
                        });
    }
}
