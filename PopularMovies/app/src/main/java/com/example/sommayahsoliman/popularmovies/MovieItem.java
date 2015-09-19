package com.example.sommayahsoliman.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sommayahsoliman on 8/10/15.
 */
public class MovieItem implements Parcelable{
    private final String name;
    private final String path;
    private final String releaseDate;
    private final String overView;
    private final double vote;
    private final int id;
    private final double popularity;
    private Extras extra; //trailers and reviews
    private boolean favorite;

    MovieItem(int id,String name, String path, String releaseDate, double vote, String overView, double popularity){
        this.id = id;
        this.name = name;
        this.path = path;
        this.releaseDate = releaseDate;
        this.vote = vote;
        this.overView = overView;
        this.popularity = popularity;
        this.favorite = false;

    }

    MovieItem(int id,String name, String path, String releaseDate, double vote, String overView,double popularity, boolean favorite,Extras extra){
        this.id = id;
        this.name = name;
        this.path = path;
        this.releaseDate = releaseDate;
        this.vote = vote;
        this.overView = overView;
        this.popularity = popularity;
        this.favorite = favorite;
        this.extra = extra;

    }





    private MovieItem(Parcel in){
        id = in.readInt();
        name = in.readString();
        path = in.readString();
        releaseDate = in.readString();
        vote = in.readDouble();
        overView=in.readString();
        popularity=in.readDouble();
        extra = in.readParcelable(Extras.class.getClassLoader());

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(releaseDate);
        dest.writeDouble(vote);
        dest.writeString(overView);
        dest.writeDouble(popularity);
        dest.writeParcelable(extra,0);


    }
    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel parcel) {
            return new MovieItem(parcel);
        }

        @Override
        public MovieItem[] newArray(int i) {
            return new MovieItem[i];
        }

    };

    public void setExtras(Extras extra){
        this.extra = extra;

    }
    public void setFavorite(boolean favorite){
        this.favorite = favorite;
    }

    public String getName(){return name;}
    public String getPath(){return path;}
    public double getVote(){
        return vote;
    }
    public String getOverView(){
        return overView;
    }
    public String getReleaseDate(){
        return releaseDate;
    }
    public int getId(){return id;}
    public Extras getExtra(){return extra;}
    public boolean getFavorite(){return favorite;}
    public double getPopularity(){return popularity;}
}
