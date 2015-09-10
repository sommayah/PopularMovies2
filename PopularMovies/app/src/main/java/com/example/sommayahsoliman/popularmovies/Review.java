package com.example.sommayahsoliman.popularmovies;

/**
 * Created by sommayahsoliman on 9/9/15.
 */
public class Review {
    private String author;
    private String body;

    Review(String author,String body){
        this.author = author;
        this.body = body;
    }

    public String getAuthor(){return author;}
    public String getBody(){return body;}
}
