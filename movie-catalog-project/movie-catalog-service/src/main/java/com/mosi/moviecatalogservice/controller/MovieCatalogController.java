package com.mosi.moviecatalogservice.controller;


import com.mosi.moviecatalogservice.domain.CatalogItem;
import com.mosi.moviecatalogservice.domain.Movie;
import com.mosi.moviecatalogservice.domain.Rating;
import com.mosi.moviecatalogservice.domain.Ratings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/catalog")
public class MovieCatalogController {

    @Autowired
    private RestTemplate restTemplate;

    //new way of calling outside services like restTemplate (will be deperciated soon)
//    @Autowired
//    private WebClient.Builder webClientBuilder;

    //lets use restTemplate to get movie info from movie-info-service and its rating from rating service
    //RestTemplates helps us to request rest api from another application,
    // it populates the payload from the api to the class we provide as an argument
     @GetMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){

         // coz our restTemplate is loadBalancer, we do not need exact url of remote server, so we change localhost:8081 to just name of the service
         //then restTemplate will ask eureka for an address of the services (becomes automatic)
         Ratings ratings= restTemplate.getForObject("http://rating-data-service/rating/user/"+ userId, Ratings.class);
        return ratings.getRatings().stream().map(rating-> {
            Movie movie= restTemplate.getForObject("http://movie-info-service/movie/"+ rating.getMovieId(), Movie.class);
/////Asynchronous way of getting movie from movie info service
//            Movie movie= webClientBuilder.build()
//                    .get()
//                    .uri("http://localhost:8081/movie/"+ rating.getMovieId())
//                    .retrieve()
//                    .bodyToMono(Movie.class)// mono is empty container that waits for payload(like promise, its asynch)
//                    .block(); // Change it to synchronous from asynchronous
            return new CatalogItem(movie.getName(), "Desc", rating.getRating());
        }).collect(Collectors.toList());
    }
}
