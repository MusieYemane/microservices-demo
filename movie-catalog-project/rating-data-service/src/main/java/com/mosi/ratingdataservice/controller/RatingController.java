package com.mosi.ratingdataservice.controller;

import com.mosi.ratingdataservice.domain.Rating;
import com.mosi.ratingdataservice.domain.Ratings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/rating")
public class RatingController {
    @GetMapping("/{movieId}")
    public Rating getRating(@PathVariable("movieId") String movieId){
        return new Rating(movieId, 3);
    }

    //Returning object instead of list helps for the frontend
    // if frontend always expects List and if modify the backend to store new object, then the frontend might crush.
    @GetMapping("/user/{userId}")
    public Ratings getUserRatings(@PathVariable("userId") String userId){
        return new Ratings(Arrays.asList(
                new Rating("1", 4),
                new Rating("2", 3)
        ));
    }
}
