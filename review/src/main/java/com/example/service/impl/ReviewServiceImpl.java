package com.example.service.impl;

import com.example.model.Review;
import com.example.payload.dto.ReviewRequest;
import com.example.payload.dto.SalonDTO;
import com.example.payload.dto.UserDTO;
import com.example.repository.ReviewRepository;
import com.example.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    @Override
    public Review createReview(ReviewRequest req, UserDTO user, SalonDTO salon) {
        Review review=new Review();
        review.setReviewText(req.getReviewText());
        review.setRating(req.getRating());
        review.setUserId(user.getId());
        review.setSalonId(salon.getId());
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsBySalonId(Long salonId) {
        return reviewRepository.findBySalonId(salonId);
    }

    private Review getReviewById(Long id) throws Exception {
        return reviewRepository.findById(id).orElseThrow(()->new Exception("Review does not exist"));
    }
    @Override
    public Review updateReview(ReviewRequest req, Long reviewId, Long userId) throws Exception {
        Review review=getReviewById(reviewId);
        if(!review.getUserId().equals(userId)){
            throw new Exception("You dont have permission to update this review.");
        }
        review.setReviewText(review.getReviewText());
        review.setRating(req.getRating());
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) throws Exception {
        Review review=getReviewById(reviewId);
        if(!review.getUserId().equals(userId)){
            throw new Exception("You dont have permission to delete this review.");
        }
        reviewRepository.delete(review);
    }
}
