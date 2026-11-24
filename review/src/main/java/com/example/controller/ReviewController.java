package com.example.controller;

import com.example.model.Review;
import com.example.payload.dto.ApiResponse;
import com.example.payload.dto.ReviewRequest;
import com.example.payload.dto.SalonDTO;
import com.example.payload.dto.UserDTO;
import com.example.service.ReviewService;
import com.example.service.client.SalonFeignClient;
import com.example.service.client.UserFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews/")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final UserFeignClient userFeignClient;
    private final SalonFeignClient salonFeignClient;

    @PostMapping("/salon/{salonId}")
    public ResponseEntity<Review> createReview(@PathVariable Long salonId,@RequestBody ReviewRequest req, @RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user= userFeignClient.getUserProfile(jwt).getBody();
        SalonDTO salon=salonFeignClient.getSalonById(salonId).getBody();

        Review review=reviewService.createReview(req,user,salon);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/salon/{salonId}")
    public ResponseEntity<List<Review>> createReview(@PathVariable Long salonId, @RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user= userFeignClient.getUserProfile(jwt).getBody();
        SalonDTO salon=salonFeignClient.getSalonById(salonId).getBody();

        List<Review> reviews=reviewService.getReviewsBySalonId(salon.getId());
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest req,@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user= userFeignClient.getUserProfile(jwt).getBody();

        Review reviews=reviewService.updateReview(req,reviewId,user.getId());
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable Long reviewId,@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user= userFeignClient.getUserProfile(jwt).getBody();

        reviewService.deleteReview(reviewId,user.getId());
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setMessage("Review Deleted");
        return ResponseEntity.ok(apiResponse);
    }
}
