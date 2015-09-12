package com.example.sommayahsoliman.popularmovies;

/**
 * Created by sommayahsoliman on 9/11/15.
 */
public class Extras{
    private Trailer[] trailers;
    private Review[] reviews;
    Extras(Trailer[] trailers,Review[] reviews){
        this.trailers = trailers.clone();
        this.reviews = reviews.clone();
    }
    public int getTrailersNum(){return trailers.length;}
    public int getReviewsNum(){return reviews.length;}
    public Trailer getTrailerAtIndex(int i){return trailers[i];}
    public Review getReviewAtIndex(int i){return reviews[i];}


}