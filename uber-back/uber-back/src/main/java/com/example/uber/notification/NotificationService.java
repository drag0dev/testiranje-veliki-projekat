package com.example.uber.notification;

import com.example.uber.ride.Ride;

/**
 * Side-effect port for ride notifications/emails. Kept as a thin interface so
 * service-layer unit tests can mock it instead of exercising real email/notification
 * infrastructure, which is out of scope for this checkout.
 */
public interface NotificationService {

    void notifyRideFinished(Ride ride);
}
