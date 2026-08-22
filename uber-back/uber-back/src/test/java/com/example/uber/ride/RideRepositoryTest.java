package com.example.uber.ride;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRepository;
import com.example.uber.user.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RideRepositoryTest {

    @Autowired private RideRepository rideRepository;
    @Autowired private UserRepository userRepository;

    private User passenger;
    private User driver;

    @BeforeEach
    void setUp() {
        passenger = userRepository.save(TestDataFactory.user("passenger@test.com", UserRole.PASSENGER));
        driver = userRepository.save(TestDataFactory.user("driver@test.com", UserRole.DRIVER));
    }

    @Test
    void findByDriverIdAndStatus_returnsOnlyMatchingRides() {
        Ride active = TestDataFactory.ride(passenger, driver, RideStatus.ACTIVE);
        Ride finished = TestDataFactory.ride(passenger, driver, RideStatus.FINISHED);
        rideRepository.save(active);
        rideRepository.save(finished);

        var result = rideRepository.findByDriverIdAndStatus(driver.getId(), RideStatus.ACTIVE);

        assertThat(result).extracting(Ride::getStatus).containsOnly(RideStatus.ACTIVE);
        assertThat(result).hasSize(1);
    }

    @Test
    void findByDriverIdAndStatus_returnsEmptyWhenNoRideMatchesStatus() {
        rideRepository.save(TestDataFactory.ride(passenger, driver, RideStatus.FINISHED));

        var result = rideRepository.findByDriverIdAndStatus(driver.getId(), RideStatus.ACTIVE);

        assertThat(result).isEmpty();
    }

    @Test
    void findByDriverIdAndStatus_returnsEmptyForUnknownDriver() {
        var result = rideRepository.findByDriverIdAndStatus(-1, RideStatus.ACTIVE);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByDriverIdAndStatusOrderByScheduledTimeAsc_returnsEarliestScheduledRide() {
        Ride later = TestDataFactory.ride(passenger, driver, RideStatus.ACCEPTED);
        later.setScheduledTime(LocalDateTime.now().plusHours(3));
        Ride earlier = TestDataFactory.ride(passenger, driver, RideStatus.ACCEPTED);
        earlier.setScheduledTime(LocalDateTime.now().plusHours(1));
        rideRepository.save(later);
        rideRepository.save(earlier);

        var result =
                rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(
                        driver.getId(), RideStatus.ACCEPTED);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(earlier.getId());
    }

    @Test
    void findFirstByDriverIdAndStatusOrderByScheduledTimeAsc_returnsEmptyWhenDriverHasNoSuchRide() {
        rideRepository.save(TestDataFactory.ride(passenger, driver, RideStatus.ACTIVE));

        var result =
                rideRepository.findFirstByDriverIdAndStatusOrderByScheduledTimeAsc(
                        driver.getId(), RideStatus.ACCEPTED);

        assertThat(result).isEmpty();
    }
}
