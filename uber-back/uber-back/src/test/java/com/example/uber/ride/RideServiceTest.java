package com.example.uber.ride;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.uber.common.exception.ConflictException;
import com.example.uber.common.exception.ForbiddenException;
import com.example.uber.common.exception.NotFoundException;
import com.example.uber.driver.DriverDetails;
import com.example.uber.driver.DriverDetailsRepository;
import com.example.uber.driver.DriverStatus;
import com.example.uber.notification.NotificationService;
import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private DriverDetailsRepository driverDetailsRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private RideService rideService;

    private User passenger;
    private User driver;
    private Ride activeRide;

    @BeforeEach
    void setUp() {
        passenger = TestDataFactory.user("alice@example.com", UserRole.PASSENGER);
        passenger.setId(1);
        driver = TestDataFactory.user("driver1@rideapp.com", UserRole.DRIVER);
        driver.setId(2);
        activeRide = TestDataFactory.ride(passenger, driver, RideStatus.ACTIVE);
        activeRide.setId(10);
    }

    @Test
    void finishRide_withNoUpcomingRide_marksRideFinishedAndDriverAvailable() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(2, RideStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        DriverDetails details = new DriverDetails(driver, DriverStatus.DRIVING);
        when(driverDetailsRepository.findByUserId(2)).thenReturn(Optional.of(details));

        Ride result = rideService.finishRide(10, "driver1@rideapp.com");

        assertThat(result.getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(result.getEndTime()).isNotNull();
        assertThat(details.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        verify(driverDetailsRepository).save(details);
        verify(notificationService, times(1)).notifyRideFinished(result);
    }

    @Test
    void finishRide_withUpcomingAcceptedRide_leavesDriverStatusUnchanged() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Ride nextRide = TestDataFactory.ride(passenger, driver, RideStatus.ACCEPTED);
        when(rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(2, RideStatus.ACCEPTED))
                .thenReturn(Optional.of(nextRide));

        Ride result = rideService.finishRide(10, "driver1@rideapp.com");

        assertThat(result.getStatus()).isEqualTo(RideStatus.FINISHED);
        verify(driverDetailsRepository, never()).findByUserId(any());
        verify(driverDetailsRepository, never()).save(any());
    }

    @Test
    void finishRide_whenDriverHasNoDriverDetailsRow_stillFinishesRideWithoutThrowing() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(2, RideStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        when(driverDetailsRepository.findByUserId(2)).thenReturn(Optional.empty());

        Ride result = rideService.finishRide(10, "driver1@rideapp.com");

        assertThat(result.getStatus()).isEqualTo(RideStatus.FINISHED);
        verify(driverDetailsRepository, never()).save(any());
    }

    @Test
    void finishRide_rideDoesNotExist_throwsNotFoundException() {
        when(rideRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.finishRide(999, "driver1@rideapp.com"))
                .isInstanceOf(NotFoundException.class);

        verify(notificationService, never()).notifyRideFinished(any());
    }

    @Test
    void finishRide_callerIsNotTheAssignedDriver_throwsForbiddenException() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));

        assertThatThrownBy(() -> rideService.finishRide(10, "someone-else@rideapp.com"))
                .isInstanceOf(ForbiddenException.class);

        verify(rideRepository, never()).save(any());
        verify(notificationService, never()).notifyRideFinished(any());
    }

    @Test
    void finishRide_rideHasNoDriverAssignedAtAll_throwsForbiddenException() {
        activeRide.setDriver(null);
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));

        assertThatThrownBy(() -> rideService.finishRide(10, "driver1@rideapp.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    @ParameterizedTest
    @EnumSource(
            value = RideStatus.class,
            names = {"ACTIVE"},
            mode = EnumSource.Mode.EXCLUDE)
    void finishRide_rideNotActive_throwsConflictException(RideStatus nonActiveStatus) {
        activeRide.setStatus(nonActiveStatus);
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));

        assertThatThrownBy(() -> rideService.finishRide(10, "driver1@rideapp.com"))
                .isInstanceOf(ConflictException.class);

        verify(rideRepository, never()).save(any());
        verify(notificationService, never()).notifyRideFinished(any());
    }

    @Test
    void finishRide_driverEmailMatchIsCaseInsensitive() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(activeRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(2, RideStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        when(driverDetailsRepository.findByUserId(2)).thenReturn(Optional.empty());

        Ride result = rideService.finishRide(10, "DRIVER1@RIDEAPP.COM");

        assertThat(result.getStatus()).isEqualTo(RideStatus.FINISHED);
    }
}
