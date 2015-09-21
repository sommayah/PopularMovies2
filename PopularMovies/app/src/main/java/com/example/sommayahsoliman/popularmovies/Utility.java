package com.example.sommayahsoliman.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sommayahsoliman.popularmovies.data.MovieContract;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sommayahsoliman on 9/16/15.
 */
public class Utility {
    private static final String IMAGE_SIZE = "w342";
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static Set<String> favoriteMovies;

    public static String getSortByCriteria(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String sort_by = sharedPref.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));
        return sort_by;
    }

    public static void setImageResource(Context context,ImageView image,String image_path){
        if(OnlineUtils.isOnline((Activity)context) == false){
            Toast.makeText(context, "no internet connection",
                    Toast.LENGTH_SHORT).show();
        }else {
            new DownloadImageTask(image)
                    .execute(BASE_URL + IMAGE_SIZE + image_path);
        }

    }

    public static Set<String> getFavoriteMovies(Context context){
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        favoriteMovies = prefs.getStringSet("favorite",new HashSet<String>());
        return favoriteMovies;
    }

    public static boolean isFavorite(Context context,String movie_id){
        if(getFavoriteMovies(context) != null){
            return favoriteMovies.contains(movie_id);
        }else
            return false;
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static void addTrailer(String movie_id, String trailerName, String trailerSource, Context context) {
        // Students: First, check if the trailer exists in the db
        // If it exists, return the current ID

        Uri trailerUri = MovieContract.TrailerEntry.CONTENT_URI;

            // Otherwise, insert it using the content resolver and the base URI
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movie_id);
            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailerName);
            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_SOURCE, trailerSource);
            Uri uri = context.getContentResolver().insert(trailerUri, cv);



    }

    public static void addReview(String movie_id, String reviewAuthor, String reviewBody, Context context) {
        // Students: First, check if the review exists in the db
        // If it exists, return the current ID

        Uri reviewUri = MovieContract.ReviewEntry.CONTENT_URI;
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie_id);
            cv.put(MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR, reviewAuthor);
            cv.put(MovieContract.ReviewEntry.COLUMN_REVIEW_BODY, reviewBody);
            Uri uri = context.getContentResolver().insert(reviewUri, cv);


    }


}
