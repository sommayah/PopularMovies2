package com.example.sommayahsoliman.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sommayahsoliman.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

/**
 * Created by sommayahsoliman on 9/17/15.
 */
//fetching favorite movies by id
public class FetchFavoritesTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchFavoritesTask.class.getSimpleName();
    private final Context mContext;
    Set<String>favoriteMoviesIds;

    public FetchFavoritesTask(Context context) {
        mContext = context;
    }

    public long addMovie(String movie_id,String name, String path, String releaseDate, double vote, String overView, double popularity,Context context) {
        // Students: First, check if the trailer exists in the db
        // If it exists, return the current ID
        long movieId;
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        Cursor cur = context.getContentResolver().query(movieUri,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{movie_id}, null);

        if (cur.moveToFirst()) {
            //location found
            int movieIdIndex = cur.getColumnIndex(MovieContract.MovieEntry._ID);
            movieId = cur.getLong(movieIdIndex);
        } else {
            // Otherwise, insert it using the content resolver and the base URI
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_KEY, movie_id);
            movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, name);
            movieValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_PATH, path);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE, vote);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overView);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, true);
            Uri uri = context.getContentResolver().insert(movieUri, movieValues);

            movieId = ContentUris.parseId(uri);
        }
        return movieId;
    }



    private void getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_ID = "id";
        final String MOVIE_TRAILER = "trailers";
        final String MOVIE_REVIEWS = "reviews";
        final String YOU_TUBE = "youtube";
        final String REVIEW_RESULTS = "results";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_PATH = "poster_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_VOTE = "vote_average";
        final String MOVIE_POPULARITY = "popularity";



        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONObject movieTrailers = movieJson.getJSONObject(MOVIE_TRAILER);
        JSONArray movieYoutubeTrailers = movieTrailers.getJSONArray(YOU_TUBE);
        JSONObject movieReviews = movieJson.getJSONObject(MOVIE_REVIEWS);
        JSONArray moviesReviewsArray = movieReviews.getJSONArray(REVIEW_RESULTS);
        Trailer[] trailers = new Trailer[movieYoutubeTrailers.length()];
        Review[] reviews = new Review[moviesReviewsArray.length()];

        String movie_title;
        String movie_path;
        String movie_overview;
        double movie_vote;
        String movie_date;
        double movie_popularity;
        int movie_id;
        // Get the JSON object representing the movie title and path
        movie_id = movieJson.getInt(MOVIE_ID);
        movie_title = movieJson.getString(MOVIE_TITLE);
        movie_path = movieJson.getString(MOVIE_PATH);
        movie_overview = movieJson.getString(MOVIE_OVERVIEW);
        movie_vote = movieJson.getDouble(MOVIE_VOTE);
        movie_popularity = movieJson.getDouble(MOVIE_POPULARITY);
        movie_date = movieJson.getString(MOVIE_RELEASE_DATE);

       addMovie(String.valueOf(movie_id),movie_title,movie_path,movie_date,movie_vote,movie_overview,movie_popularity,mContext);

        for(int i=0; i<movieYoutubeTrailers.length();i++){
            JSONObject trailer = movieYoutubeTrailers.getJSONObject(i);
            trailers[i] = new Trailer(trailer.getString("name"),trailer.getString("source"));
            // Log.v(LOG_TAG,i+":"+trailers[i].getSource());
            Utility.addTrailer(String.valueOf(movie_id),trailers[i].getTitle(),trailers[i].getSource(),mContext);
        }
        for(int i=0; i<moviesReviewsArray.length();i++){
            JSONObject review = moviesReviewsArray.getJSONObject(i);
            reviews[i] = new Review(review.getString("author"),review.getString("content"));
            //  Log.v(LOG_TAG,i+":"+reviews[i].getBody());
            Utility.addReview(String.valueOf(movie_id),reviews[i].getAuthor(),reviews[i].getBody(),mContext);
        }






    }

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        String format = "json";
        String api = ApiKey.API_KEY;
        String trailerandreviews = "trailers,reviews";


        try {

            // Construct the URL for the Movie query

            // themoviedb.org
            //the string parameter here is the movie id
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/"+ params[0] +"?";
            final String API_PARAM = "api_key";
            final String APPEND_PARAM = "append_to_response";
            //URL url = new URL("http://api.themoviedb.org/3/movie/id?&api_key=[YOUR API KEY]");
            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(API_PARAM, api)
                    .appendQueryParameter(APPEND_PARAM,trailerandreviews)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built uri " + builtUri.toString());

            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;

        } catch (JSONException e) {
            e.printStackTrace();
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }


        return null;
    }



}
