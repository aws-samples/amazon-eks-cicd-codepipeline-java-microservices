package com.amazonaws.awssamples.microservices.review.service;

import com.amazonaws.awssamples.microservices.common.Review;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value="/service/review")
public class ReviewService {

    private Map<Long, Review> productReviewMap;

    public ReviewService() {
        this.productReviewMap = new HashMap<>();
        this.productReviewMap.put(1L, new Review(1, "Review for Book1"));
        this.productReviewMap.put(2L, new Review(2, "Review for Book2"));
        this.productReviewMap.put(3L, new Review(3, "Review for Book3"));
        this.productReviewMap.put(4L, new Review(4, "Review for Book4"));
    }

    @RequestMapping(value="/v1/{productId}",method = RequestMethod.GET)
    public Review getReview(@PathVariable Long productId){
        return productReviewMap.get(productId);
    }
}
