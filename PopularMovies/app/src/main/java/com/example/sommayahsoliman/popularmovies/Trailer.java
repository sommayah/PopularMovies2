package com.example.sommayahsoliman.popularmovies;

/**
 * Created by sommayahsoliman on 9/9/15.
 */
public class Trailer {
    private String title;
    private String source;

    Trailer(String title,String source){
        this.title = title;
        this.source = source;
    }

    public String getTitle(){return title;}
    public String getSource(){return source;}
}
