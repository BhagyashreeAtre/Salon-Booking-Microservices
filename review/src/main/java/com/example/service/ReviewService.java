package com.example.service;

import com.example.model.Review;
import com.example.payload.dto.ReviewRequest;
import com.example.payload.dto.SalonDTO;
import com.example.payload.dto.UserDTO;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewRequest req, UserDTO user, SalonDTO salon);

    List<Review> getReviewsBySalonId(Long salonId);

    Review updateReview(ReviewRequest req,Long reviewId,Long userId) throws Exception;

    void deleteReview(Long reviewId,Long userId) throws Exception;
}
