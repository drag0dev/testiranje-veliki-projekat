package com.example.uber.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRepository;
import com.example.uber.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DriverDetailsRepositoryTest {

    @Autowired private DriverDetailsRepository driverDetailsRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByUserId_returnsDetailsWhenPresent() {
        User driver = userRepository.save(TestDataFactory.user("driver@test.com", UserRole.DRIVER));
        driverDetailsRepository.save(new DriverDetails(driver, DriverStatus.DRIVING));

        var result = driverDetailsRepository.findByUserId(driver.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(DriverStatus.DRIVING);
    }

    @Test
    void findByUserId_returnsEmptyWhenUserHasNoDriverDetails() {
        User passenger =
                userRepository.save(TestDataFactory.user("passenger@test.com", UserRole.PASSENGER));

        var result = driverDetailsRepository.findByUserId(passenger.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_returnsEmptyForUnknownId() {
        var result = driverDetailsRepository.findByUserId(-1);

        assertThat(result).isEmpty();
    }
}
