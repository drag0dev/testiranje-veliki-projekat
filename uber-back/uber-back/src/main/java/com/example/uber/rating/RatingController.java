package com.example.uber.rating;

import com.example.uber.rating.dto.RatingRequest;
import com.example.uber.rating.dto.RatingResponse;
import com.example.uber.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/{id}/rating")
    @PreAuthorize("hasRole('PASSENGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse rate(
            @PathVariable Integer id,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Rating rating = ratingService.submitRating(id, principal.getUsername(), request);
        return RatingResponse.from(rating);
    }
}
