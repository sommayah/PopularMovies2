package com.example.sommayahsoliman.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private final String IMAGE_SIZE = "w342";
    private final String RELEASE_DATE = "Release Date: ";
    private final String VOTE = "Vote: ";
    private final String BASE_URL = "http://image.tmdb.org/t/p/";
    static final String DETAIL_INTENT = "detail_movie";
    static final String DETAIL_URI = "Uri";
    private String name;
    private String path;
    private String release_date;
    private double vote;
    private String overview;
    private int movie_id;
    private Intent mIntent; //will be changed to uri later
    private Extras extras; //this includes trailers and reviews

    public DetailActivityFragment() {
        setHasOptionsMenu(false);
    }

    public static DetailActivityFragment newInstance(int index){
        DetailActivityFragment f = new DetailActivityFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    public int getShownIndex(){
        return getArguments().getInt("index",0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        Intent intent = new Intent();
        if(arguments != null){
            intent = arguments.getParcelable(DetailActivityFragment.DETAIL_INTENT);
        }

        if(intent != null && intent.hasExtra("title")){
            name = intent.getStringExtra("title");
            path = intent.getStringExtra("path");
            release_date = intent.getStringExtra("release_date");
            vote = intent.getDoubleExtra("vote", 0);
            overview = intent.getStringExtra("overview");
            movie_id = intent.getIntExtra("movie_id",0);
            TextView textView = (TextView)rootView.findViewById(R.id.textViewTitle);
            textView.setText(name);
            TextView dateTextView = (TextView)rootView.findViewById(R.id.textViewDate);
            dateTextView.setText(RELEASE_DATE+release_date);
            TextView voteTextView = (TextView)rootView.findViewById(R.id.textViewVote);
            voteTextView.setText(VOTE + String.valueOf(vote));
            TextView overviewTextView = (TextView)rootView.findViewById(R.id.textViewOverView);
            overviewTextView.setText(overview);
            ImageView imageView = (ImageView)rootView.findViewById(R.id.imageView);
            Button favorite_btn = (Button) rootView.findViewById(R.id.favorite_btn);
            favorite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFavoriteClick();
                }
            });
            if(OnlineUtils.isOnline(getActivity()) == false){
                Toast.makeText(getActivity(), "no internet connection",
                        Toast.LENGTH_SHORT).show();
            }else {
                new DownloadImageTask(imageView)
                        .execute(BASE_URL + IMAGE_SIZE + path);
            }

            if(OnlineUtils.isOnline(getActivity()) == false){
                Toast.makeText(getActivity(), "no internet connection",
                        Toast.LENGTH_SHORT).show();
            }else {
                new FetchExtrasTask().execute();

            }



        }
        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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

    public void onFavoriteClick(){
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> favoriteMovies;
        Set<String> set = new HashSet<String>();
        //converting the id's to strings to be able to save them in a set of strings
        favoriteMovies = prefs.getStringSet("favorite", new HashSet<String>());
        String id_string = String.valueOf(movie_id);
        if (!favoriteMovies.contains(id_string)) {
            favoriteMovies.add(id_string);
        } else {
            favoriteMovies.remove(id_string); //if user press twice on favorite the movie get removed from favorites
        }
        editor.putStringSet("favorite", favoriteMovies);
        editor.commit();
    }

    public class FetchExtrasTask extends AsyncTask<Void, Void, Extras> {

        private final String LOG_TAG = FetchExtrasTask.class.getSimpleName();



        private Extras getExtraDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_ID = "id";
            final String MOVIE_TRAILER = "trailers";
            final String MOVIE_REVIEWS = "reviews";
            final String YOU_TUBE = "youtube";
            final String REVIEW_RESULTS = "results";



            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONObject movieTrailers = movieJson.getJSONObject(MOVIE_TRAILER);
            JSONArray movieYoutubeTrailers = movieTrailers.getJSONArray(YOU_TUBE);
            JSONObject movieReviews = movieJson.getJSONObject(MOVIE_REVIEWS);
            JSONArray moviesReviewsArray = movieReviews.getJSONArray(REVIEW_RESULTS);
            //movie title and movie poster path FOR LOG PURPOSES ONLY
            Trailer[] trailers = new Trailer[movieYoutubeTrailers.length()];
            Review[] reviews = new Review[moviesReviewsArray.length()];

            for(int i=0; i<movieYoutubeTrailers.length();i++){
                JSONObject trailer = movieYoutubeTrailers.getJSONObject(i);
                trailers[i] = new Trailer(trailer.getString("name"),trailer.getString("source"));
                Log.v(LOG_TAG,i+":"+trailers[i].getSource());
            }
            for(int i=0; i<moviesReviewsArray.length();i++){
                JSONObject review = moviesReviewsArray.getJSONObject(i);
                reviews[i] = new Review(review.getString("author"),review.getString("content"));
                Log.v(LOG_TAG,i+":"+reviews[i].getBody());
            }


//            for(int i = 0; i < movieArray.length(); i++) {
//                String movie_title;
//                String movie_path;
//                String movie_overview;
//                double movie_vote;
//                String movie_date;
//                int movie_id;
//                // Get the JSON object representing the movie title and path
//                JSONObject movie = movieArray.getJSONObject(i);
//                movie_id = movie.getInt(MOVIE_ID);
//                movie_title = movie.getString(MOVIE_TITLE);
//                movie_path = movie.getString(MOVIE_PATH);
//                movie_overview=movie.getString(MOVIE_OVERVIEW);
//                movie_vote = movie.getDouble(MOVIE_VOTE);
//                movie_date = movie.getString(MOVIE_RELEASE_DATE);
//                resultTitles[i] = movie_title;
//                resultPaths[i] = movie_path;
//
//                movieItems.add(new MovieItem(movie_id,movie_title,movie_path,movie_date,movie_vote,movie_overview));
//            }


            extras = new Extras(trailers,reviews);

            return extras;

        }

        @Override
        protected Extras doInBackground(Void... params) {

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


                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/"+ movie_id +"?";
                final String API_PARAM = "api_key";
                final String APPEND_PARAM = "append_to_response";
                //URL url = new URL("http://api.themoviedb.org/3/movie/id?&api_key=[YOUR API KEY]");
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(API_PARAM, api)
                        .appendQueryParameter(APPEND_PARAM,trailerandreviews)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG,"Built uri "+ builtUri.toString());

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

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;

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

            try {
                Log.v(LOG_TAG,movieJsonStr);
                return getExtraDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Extras extras) {
            if (extras != null) {
                //update ui
                super.onPostExecute(extras);
            }
        }
    }

}

 class Extras{
     private Trailer[] trailers;
     private Review[] reviews;
    Extras(Trailer[] trailers,Review[] reviews){
        this.trailers = trailers.clone();
        this.reviews = reviews.clone();
    }
}
