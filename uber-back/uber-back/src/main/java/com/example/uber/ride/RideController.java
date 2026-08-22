package com.example.uber.ride;

import com.example.uber.ride.dto.RideResponse;
import com.example.uber.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('DRIVER')")
    public RideResponse finishRide(
            @PathVariable Integer id, @AuthenticationPrincipal UserPrincipal principal) {
        Ride ride = rideService.finishRide(id, principal.getUsername());
        return RideResponse.from(ride);
    }
}
