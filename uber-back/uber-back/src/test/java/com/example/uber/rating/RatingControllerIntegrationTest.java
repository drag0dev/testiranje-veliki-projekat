package com.example.uber.rating;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.uber.ride.Ride;
import com.example.uber.ride.RideRepository;
import com.example.uber.ride.RideStatus;
import com.example.uber.security.JwtService;
import com.example.uber.support.TestDataFactory;
import com.example.uber.user.User;
import com.example.uber.user.UserRepository;
import com.example.uber.user.UserRole;
import java.time.LocalDateTime;
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
class RatingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RideRepository rideRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private JwtService jwtService;

    private User passenger;
    private User otherPassenger;
    private User driver;

    @BeforeEach
    void setUp() {
        passenger = userRepository.save(TestDataFactory.user("passenger@test.com", UserRole.PASSENGER));
        otherPassenger =
                userRepository.save(TestDataFactory.user("other-passenger@test.com", UserRole.PASSENGER));
        driver = userRepository.save(TestDataFactory.user("driver@test.com", UserRole.DRIVER));
    }

    @Test
    void rate_finishedRideWithinWindow_returnsCreated() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));

        mockMvc
                .perform(rateRequest(finished.getId(), passenger, validPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    void rate_rideNotFinishedYet_returnsConflict() throws Exception {
        Ride active = rideRepository.save(TestDataFactory.ride(passenger, driver, RideStatus.ACTIVE));

        mockMvc.perform(rateRequest(active.getId(), passenger, validPayload())).andExpect(status().isConflict());
    }

    @Test
    void rate_ratingWindowExpired_returnsConflict() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusDays(4));

        mockMvc.perform(rateRequest(finished.getId(), passenger, validPayload())).andExpect(status().isConflict());
    }

    @Test
    void rate_rideAlreadyRated_returnsConflict() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));
        mockMvc.perform(rateRequest(finished.getId(), passenger, validPayload())).andExpect(status().isCreated());

        mockMvc.perform(rateRequest(finished.getId(), passenger, validPayload())).andExpect(status().isConflict());
    }

    @Test
    void rate_byPassengerWhoDidNotBookTheRide_isForbidden() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));

        mockMvc
                .perform(rateRequest(finished.getId(), otherPassenger, validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void rate_withOutOfRangeRating_returnsBadRequest() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));
        String invalidPayload = "{\"driverRating\":6,\"vehicleRating\":4,\"comment\":\"n/a\"}";

        mockMvc
                .perform(rateRequest(finished.getId(), passenger, invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rate_withMissingRequiredFields_returnsBadRequest() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));
        String missingFieldsPayload = "{\"comment\":\"n/a\"}";

        mockMvc
                .perform(rateRequest(finished.getId(), passenger, missingFieldsPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rate_rideDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(rateRequest(999_999, passenger, validPayload())).andExpect(status().isNotFound());
    }

    @Test
    void rate_withoutAuthentication_isRejected() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));

        mockMvc
                .perform(
                        post("/api/rides/{id}/rating", finished.getId())
                                .contentType(APPLICATION_JSON)
                                .content(validPayload()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void rate_asDriver_isForbidden() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusHours(2));

        mockMvc.perform(rateRequest(finished.getId(), driver, validPayload())).andExpect(status().isForbidden());
    }

    @Test
    void rate_justInsideThreeDayWindow_returnsCreated() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusDays(3).plusMinutes(5));

        mockMvc
                .perform(rateRequest(finished.getId(), passenger, validPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    void rate_justOutsideThreeDayWindow_returnsConflict() throws Exception {
        Ride finished = finishedRide(LocalDateTime.now().minusDays(3).minusMinutes(5));

        mockMvc.perform(rateRequest(finished.getId(), passenger, validPayload())).andExpect(status().isConflict());
    }

    private Ride finishedRide(LocalDateTime endTime) {
        Ride ride = TestDataFactory.ride(passenger, driver, RideStatus.FINISHED);
        ride.setEndTime(endTime);
        return rideRepository.save(ride);
    }

    private String validPayload() {
        return "{\"driverRating\":5,\"vehicleRating\":4,\"comment\":\"Great ride\"}";
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder rateRequest(
            Integer rideId, User actingUser, String jsonBody) {
        String token = jwtService.generateToken(actingUser);
        return post("/api/rides/{id}/rating", rideId)
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(jsonBody);
    }
}
