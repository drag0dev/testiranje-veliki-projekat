package com.example.uber.notification;

import com.example.uber.ride.Ride;

public interface NotificationService {

    void notifyRideFinished(Ride ride);
}
