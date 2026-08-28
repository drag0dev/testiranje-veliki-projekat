package com.example.uber.support;

import com.example.uber.ride.Ride;
import com.example.uber.ride.RideStatus;
import com.example.uber.user.User;
import com.example.uber.user.UserRole;
import com.example.uber.vehicle.VehicleType;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("$2b$10$64DmgG05BwAaXLhAJ/rhsu0DyF1GdolUJ3MooEeewu9Jn4U27BkuS"); // "password123"
        user.setFirstName("Test");
        user.setLastName(role.name());
        user.setRole(role);
        user.setActivated(true);
        user.setBlocked(false);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static Ride ride(User passenger, User driver, RideStatus status) {
        Ride ride = new Ride();
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setVehicleType(VehicleType.STANDARD);
        ride.setStatus(status);
        ride.setStartAddress("Zmaj Jovina 5, Novi Sad");
        ride.setEndAddress("Spens, Novi Sad");
        ride.setDistanceKm(new java.math.BigDecimal("3.20"));
        ride.setPrice(new java.math.BigDecimal("534.00"));
        ride.setCreatedAt(LocalDateTime.now());
        return ride;
    }
}
