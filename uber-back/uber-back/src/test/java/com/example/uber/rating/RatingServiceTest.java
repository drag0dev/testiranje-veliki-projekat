package com.example.uber.rating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.uber.common.exception.ConflictException;
import com.example.uber.common.exception.ForbiddenException;
import com.example.uber.common.exception.NotFoundException;
import com.example.uber.rating.dto.RatingRequest;
import com.example.uber.ride.Ride;
import com.example.uber.ride.RideRepository;
import com.example.uber.ride.RideStatus;
import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRole;
import java.time.LocalDateTime;
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
class RatingServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private RatingRepository ratingRepository;

    @InjectMocks private RatingService ratingService;

    private User passenger;
    private User otherPassenger;
    private User driver;
    private Ride finishedRide;

    @BeforeEach
    void setUp() {
        passenger = TestDataFactory.user("alice@example.com", UserRole.PASSENGER);
        passenger.setId(1);
        otherPassenger = TestDataFactory.user("bob@example.com", UserRole.PASSENGER);
        otherPassenger.setId(2);
        driver = TestDataFactory.user("driver1@rideapp.com", UserRole.DRIVER);
        driver.setId(3);
        finishedRide = TestDataFactory.ride(passenger, driver, RideStatus.FINISHED);
        finishedRide.setId(10);
        finishedRide.setEndTime(LocalDateTime.now().minusHours(2));
    }

    private RatingRequest validRequest() {
        return new RatingRequest(5, 4, "Great ride, thanks!");
    }

    @Test
    void submitRating_validRequest_savesRatingWithGivenValues() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));
        when(ratingRepository.existsByRideId(10)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.submitRating(10, "alice@example.com", validRequest());

        assertThat(result.getRide()).isEqualTo(finishedRide);
        assertThat(result.getPassenger()).isEqualTo(passenger);
        assertThat(result.getDriverRating()).isEqualTo((short) 5);
        assertThat(result.getVehicleRating()).isEqualTo((short) 4);
        assertThat(result.getComment()).isEqualTo("Great ride, thanks!");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void submitRating_rideDoesNotExist_throwsNotFoundException() {
        when(rideRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.submitRating(999, "alice@example.com", validRequest()))
                .isInstanceOf(NotFoundException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_callerIsNotTheRidePassenger_throwsForbiddenException() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));

        assertThatThrownBy(() -> ratingService.submitRating(10, "bob@example.com", validRequest()))
                .isInstanceOf(ForbiddenException.class);

        verify(ratingRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = RideStatus.class,
            names = {"FINISHED"},
            mode = EnumSource.Mode.EXCLUDE)
    void submitRating_rideNotFinished_throwsConflictException(RideStatus nonFinishedStatus) {
        finishedRide.setStatus(nonFinishedStatus);
        finishedRide.setEndTime(null);
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));

        assertThatThrownBy(() -> ratingService.submitRating(10, "alice@example.com", validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_finishedRideWithNullEndTime_throwsConflictException() {
        finishedRide.setEndTime(null);
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));

        assertThatThrownBy(() -> ratingService.submitRating(10, "alice@example.com", validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_ratingWindowExpired_throwsConflictException() {
        finishedRide.setEndTime(LocalDateTime.now().minusDays(4));
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));

        assertThatThrownBy(() -> ratingService.submitRating(10, "alice@example.com", validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_justInsideThreeDayWindow_succeeds() {
        finishedRide.setEndTime(LocalDateTime.now().minusDays(3).plusMinutes(5));
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));
        when(ratingRepository.existsByRideId(10)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.submitRating(10, "alice@example.com", validRequest());

        assertThat(result).isNotNull();
    }

    @Test
    void submitRating_justOutsideThreeDayWindow_throwsConflictException() {
        finishedRide.setEndTime(LocalDateTime.now().minusDays(3).minusMinutes(5));
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));

        assertThatThrownBy(() -> ratingService.submitRating(10, "alice@example.com", validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_rideAlreadyRated_throwsConflictException() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));
        when(ratingRepository.existsByRideId(10)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.submitRating(10, "alice@example.com", validRequest()))
                .isInstanceOf(ConflictException.class);

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void submitRating_passengerEmailMatchIsCaseInsensitive() {
        when(rideRepository.findById(10)).thenReturn(Optional.of(finishedRide));
        when(ratingRepository.existsByRideId(10)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.submitRating(10, "ALICE@EXAMPLE.COM", validRequest());

        assertThat(result).isNotNull();
    }
}
