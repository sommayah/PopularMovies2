package com.example.sommayahsoliman.popularmovies;

import android.content.ContentValues;
import android.content.Context;
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
import java.util.Vector;

/**
 * Created by sommayahsoliman on 9/17/15.
 */
public class FetchExtrasTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchExtrasTask.class.getSimpleName();
    private final Context mContext;
    private String movie_id;

    public FetchExtrasTask(Context context) {
        mContext = context;
    }



    private void getExtraDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_ID = "id";
        final String MOVIE_TRAILER = "trailers";
        final String MOVIE_REVIEWS = "reviews";
        final String YOU_TUBE = "youtube";
        final String REVIEW_RESULTS = "results";


        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONObject movieTrailers = movieJson.getJSONObject(MOVIE_TRAILER);
            JSONArray movieYoutubeTrailers = movieTrailers.getJSONArray(YOU_TUBE);
            JSONObject movieReviews = movieJson.getJSONObject(MOVIE_REVIEWS);
            JSONArray moviesReviewsArray = movieReviews.getJSONArray(REVIEW_RESULTS);
            //movie title and movie poster path FOR LOG PURPOSES ONLY
            Trailer[] trailers = new Trailer[movieYoutubeTrailers.length()];
            Review[] reviews = new Review[moviesReviewsArray.length()];

            Vector<ContentValues> cVVectorT = new Vector<ContentValues>(movieYoutubeTrailers.length());
            for (int i = 0; i < movieYoutubeTrailers.length(); i++) {
                JSONObject trailer = movieYoutubeTrailers.getJSONObject(i);
                trailers[i] = new Trailer(trailer.getString("name"), trailer.getString("source"));
                //   Log.v(LOG_TAG,i+":"+trailers[i].getSource());
                ContentValues trailerValues = new ContentValues();
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movie_id);
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailers[i].getTitle());
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_SOURCE, trailers[i].getSource());
                cVVectorT.add(trailerValues);
            }

            Vector<ContentValues> cVVectorR = new Vector<ContentValues>(moviesReviewsArray.length());
            for (int i = 0; i < moviesReviewsArray.length(); i++) {
                JSONObject review = moviesReviewsArray.getJSONObject(i);
                reviews[i] = new Review(review.getString("author"), review.getString("content"));
                //   Log.v(LOG_TAG,i+":"+reviews[i].getBody());
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movie_id);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR, reviews[i].getAuthor());
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_BODY, reviews[i].getBody());
                cVVectorR.add(reviewValues);
            }
            int inserted = 0;
            // add to database
            if (cVVectorT.size() > 0) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] values = cVVectorT.toArray(new ContentValues[cVVectorT.size()]);
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, values);
            }

            Log.d(LOG_TAG, "FetchTrailersTask Complete. " + inserted + " Inserted");

            inserted = 0;
            // add to database
            if (cVVectorR.size() > 0) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] values = cVVectorR.toArray(new ContentValues[cVVectorR.size()]);
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, values);
            }

            Log.d(LOG_TAG, "FetchReviewsTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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


            movie_id = params[0];
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/"+ movie_id +"?";
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
            getExtraDataFromJson(movieJsonStr);
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
