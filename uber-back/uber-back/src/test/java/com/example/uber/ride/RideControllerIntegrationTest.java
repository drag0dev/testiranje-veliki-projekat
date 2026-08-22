package com.example.uber.ride;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.uber.driver.DriverDetails;
import com.example.uber.driver.DriverDetailsRepository;
import com.example.uber.driver.DriverStatus;
import com.example.uber.security.JwtService;
import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRepository;
import com.example.uber.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RideControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RideRepository rideRepository;
    @Autowired private DriverDetailsRepository driverDetailsRepository;
    @Autowired private JwtService jwtService;

    private User driver;
    private User otherDriver;
    private User passenger;
    private Ride activeRide;

    @BeforeEach
    void setUp() {
        passenger = userRepository.save(TestDataFactory.user("passenger@test.com", UserRole.PASSENGER));
        driver = userRepository.save(TestDataFactory.user("driver@test.com", UserRole.DRIVER));
        otherDriver = userRepository.save(TestDataFactory.user("otherdriver@test.com", UserRole.DRIVER));
        driverDetailsRepository.save(new DriverDetails(driver, DriverStatus.DRIVING));
        driverDetailsRepository.save(new DriverDetails(otherDriver, DriverStatus.DRIVING));
        activeRide = rideRepository.save(TestDataFactory.ride(passenger, driver, RideStatus.ACTIVE));
    }

    @Test
    void finishRide_asAssignedDriver_marksRideFinishedAndReturnsIt() throws Exception {
        mockMvc
                .perform(finishRequest(activeRide.getId(), driver))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeRide.getId()))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.endTime").exists());

        assertThat(rideRepository.findById(activeRide.getId()).orElseThrow().getStatus())
                .isEqualTo(RideStatus.FINISHED);
    }

    @Test
    void finishRide_setsDriverBackToAvailableWhenNoUpcomingRide() throws Exception {
        mockMvc.perform(finishRequest(activeRide.getId(), driver)).andExpect(status().isOk());

        assertThat(driverDetailsRepository.findByUserId(driver.getId()).orElseThrow().getStatus())
                .isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void finishRide_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(post("/api/rides/{id}/finish", activeRide.getId())).andExpect(status().is4xxClientError());
    }

    @Test
    void finishRide_asPassenger_isForbidden() throws Exception {
        mockMvc.perform(finishRequest(activeRide.getId(), passenger)).andExpect(status().isForbidden());
    }

    @Test
    void finishRide_asDriverNotAssignedToThisRide_isForbidden() throws Exception {
        mockMvc.perform(finishRequest(activeRide.getId(), otherDriver)).andExpect(status().isForbidden());
    }

    @Test
    void finishRide_rideDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(finishRequest(999_999, driver)).andExpect(status().isNotFound());
    }

    @Test
    void finishRide_rideAlreadyFinished_returnsConflict() throws Exception {
        Ride finished = TestDataFactory.ride(passenger, driver, RideStatus.FINISHED);
        rideRepository.save(finished);

        mockMvc.perform(finishRequest(finished.getId(), driver)).andExpect(status().isConflict());
    }

    @Test
    void finishRide_ridePendingWithNoDriverYet_isForbidden() throws Exception {
        Ride pending = TestDataFactory.ride(passenger, null, RideStatus.PENDING);
        rideRepository.save(pending);

        mockMvc.perform(finishRequest(pending.getId(), driver)).andExpect(status().isForbidden());
    }

    @Test
    void finishRide_rideAcceptedButNotYetActive_returnsConflict() throws Exception {
        Ride accepted = TestDataFactory.ride(passenger, driver, RideStatus.ACCEPTED);
        rideRepository.save(accepted);

        mockMvc.perform(finishRequest(accepted.getId(), driver)).andExpect(status().isConflict());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder finishRequest(
            Integer rideId, User actingUser) {
        String token = jwtService.generateToken(actingUser);
        return post("/api/rides/{id}/finish", rideId).header("Authorization", "Bearer " + token);
    }
}
