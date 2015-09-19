package com.example.sommayahsoliman.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.example.sommayahsoliman.popularmovies.data.MovieContract;

import java.io.InputStream;
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

    public static void setImageResource(ImageView image,String image_path){
        new DownloadImageTask(image)
                  .execute(BASE_URL + IMAGE_SIZE + image_path);

    }

    public static Set<String> getFavoriteMovies(Context context){
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        favoriteMovies = prefs.getStringSet("favorite",null);
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

    public static long addTrailer(String movie_id, String trailerName, String trailerSource, Context context) {
        // Students: First, check if the trailer exists in the db
        // If it exists, return the current ID
        long trailerId;
        Uri trailerUri = MovieContract.TrailerEntry.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(trailerUri,
                new String[]{MovieContract.TrailerEntry._ID},
                MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{movie_id}, null);

        if (cur.moveToFirst()) {
            //location found
            int trailerIdIndex = cur.getColumnIndex(MovieContract.TrailerEntry._ID);
            trailerId = cur.getLong(trailerIdIndex);
        } else {
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movie_id);
            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailerName);
            cv.put(MovieContract.TrailerEntry.COLUMN_TRAILER_SOURCE, trailerSource);
            Uri uri = context.getContentResolver().insert(trailerUri, cv);

            trailerId = ContentUris.parseId(uri);
        }
        return trailerId;
    }

    public static long addReview(String movie_id, String reviewAuthor, String reviewBody, Context context) {
        // Students: First, check if the review exists in the db
        // If it exists, return the current ID
        long reviewId;
        Uri reviewUri = MovieContract.ReviewEntry.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(reviewUri,
                new String[]{MovieContract.ReviewEntry._ID},
                MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{movie_id}, null);

        if (cur.moveToFirst()) {

            int reviewIdIndex = cur.getColumnIndex(MovieContract.ReviewEntry._ID);
            reviewId = cur.getLong(reviewIdIndex);
        } else {
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie_id);
            cv.put(MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR, reviewAuthor);
            cv.put(MovieContract.ReviewEntry.COLUMN_REVIEW_BODY, reviewBody);
            Uri uri = context.getContentResolver().insert(reviewUri, cv);

            reviewId = ContentUris.parseId(uri);
        }
        return reviewId;
    }

}
