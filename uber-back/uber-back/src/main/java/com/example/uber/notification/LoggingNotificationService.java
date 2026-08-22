package com.example.uber.notification;

import com.example.uber.ride.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void notifyRideFinished(Ride ride) {
        log.info("Ride {} finished, passenger {} notified", ride.getId(), ride.getPassenger().getEmail());
    }
}
