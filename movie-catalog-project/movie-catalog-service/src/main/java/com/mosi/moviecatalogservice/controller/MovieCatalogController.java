package com.mosi.moviecatalogservice.controller;


import com.mosi.moviecatalogservice.domain.Movie;
import com.mosi.moviecatalogservice.domain.Rating;
import com.mosi.moviecatalogservice.domain.Ratings;
import com.mosi.moviecatalogservice.domain.CatalogItem;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
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

    //lets use restTemplate to get m ovie info from movie-info-service and its rating from rating service
    //RestTemplates helps us to request rest api from another application,
    // it populates the payload from the api to the class we provide as an argument
     @GetMapping("/{userId}")
     //Since we used 2 other microservices, we need to create a circuit breaker here(stops sending requests to very slow, or crashed microservices)
    // we will use hystrix of netflix, which is depreciated.
     //fall back method sends default custom data(from getFallbackCatalog method)  to client if the requested microservice is not responding
     @HystrixCommand(fallbackMethod="getFallbackCatalog")
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

    // this is default response to client if any microservices is down or too slow to respond
    // try this by stoping movie-info-service microservice, and request catalog api from browser)
    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId){
         return Arrays.asList(new CatalogItem("Default Movie Title", "Fallback response", 0));
    }
}
