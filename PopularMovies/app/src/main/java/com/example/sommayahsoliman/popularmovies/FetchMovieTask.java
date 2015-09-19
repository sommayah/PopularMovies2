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
public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */




    private void getMovieDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_ID = "id";
        final String MOVIE_LIST = "results";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_PATH = "poster_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_VOTE = "vote_average";
        final String MOVIE_POPULARITY = "popularity";

        try {
            JSONObject movieJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);
            //movie title and movie poster path FOR LOG PURPOSES ONLY
            String[] resultTitles = new String[movieArray.length()];
            String[] resultPaths = new String[movieArray.length()];

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                String movie_title;
                String movie_path;
                String movie_overview;
                double movie_vote;
                double movie_popularity;
                String movie_date;
                int movie_id;
                // Get the JSON object representing the movie title and path
                JSONObject movie = movieArray.getJSONObject(i);
                movie_id = movie.getInt(MOVIE_ID);
                movie_title = movie.getString(MOVIE_TITLE);
                movie_path = movie.getString(MOVIE_PATH);
                movie_overview = movie.getString(MOVIE_OVERVIEW);
                movie_vote = movie.getDouble(MOVIE_VOTE);
                movie_date = movie.getString(MOVIE_RELEASE_DATE);
                movie_popularity = movie.getDouble(MOVIE_POPULARITY);
                resultTitles[i] = movie_title;
                resultPaths[i] = movie_path;

             //   getExtrasInBackground(String.valueOf(movie_id));

                MovieItem movieItem = new MovieItem(movie_id, movie_title, movie_path, movie_date, movie_vote, movie_overview, movie_popularity);
               // movieItem.setFavorite(isFavorite(String.valueOf(movie_id)));

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_KEY, movie_id);
                movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, movie_title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_PATH, movie_path);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie_date);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE, movie_vote);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie_overview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie_popularity);
                movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, Utility.isFavorite(mContext,String.valueOf(movie_id)));
                cVVector.add(movieValues);

            }


            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] values = cVVector.toArray(new ContentValues[cVVector.size()]);
                inserted = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, values);
            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

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


        try {

            // Construct the URL for the Movie query

            // themoviedb.org


            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_PARAM = "api_key";
            //URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]");
            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM,params[0])
                    .appendQueryParameter(API_PARAM, api)
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




    private void getExtrasInBackground(String movie_id) {
        FetchExtrasTask extrasTask = new FetchExtrasTask(mContext);
        extrasTask.execute(movie_id);

    }
}


