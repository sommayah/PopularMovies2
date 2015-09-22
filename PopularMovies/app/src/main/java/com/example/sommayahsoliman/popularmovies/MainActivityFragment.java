package com.example.sommayahsoliman.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment{
    ArrayList<MovieItem> movieItems;
    ArrayList<Extras> extrasArray;
    ImageAdapter adapter;
    Set<String> favoriteMovies;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private GridView mGridView;
    private static String sort_by;

    private static final int MY_LOADER_ID = 0;


    private static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_KEY,
            MovieContract.MovieEntry.COLUMN_NAME,
            MovieContract.MovieEntry.COLUMN_IMAGE_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_VOTE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_FAVORITE

    };

    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME+ "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_MOVIE_KEY,
            MovieContract.TrailerEntry.COLUMN_TRAILER_NAME,
            MovieContract.TrailerEntry.COLUMN_TRAILER_SOURCE
    };

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME+ "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY,
            MovieContract.ReviewEntry.COLUMN_REVIEW_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_REVIEW_BODY
    };



    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_KEY = 1;
    static final int COL_MOVIE_NAME = 2;
    static final int COL_MOVIE_IMAGE_PATH = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_OVERVIEW = 5;
    static final int COL_MOVIE_VOTE = 6;
    static final int COL_MOVIE_POPULARITY = 7;
    static final int COL_MOVIE_FAVORITE = 8;

    static final int COL_TRAILER_NAME = 2;
    static final int COL_TRAILER_SOURCE = 3;

    static final int COL_REVIEW_NAME = 2;
    static final int COL_REVIEW_BODY = 3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                onChangedSelection();

            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);

        if(savedInstanceState != null && savedInstanceState.containsKey("movies")) {
             movieItems = savedInstanceState.getParcelableArrayList("movies");
        }else{
            updateMovies();
        }
        //to listen to setting changes and fetch new data when changed


    }

    void onChangedSelection(){
        updateMovies();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieItems);
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    void updateMovies(){
        //strings: vote_average.desc, popularity.desc

        sort_by = Utility.getSortByCriteria(getActivity());
        favoriteMovies = Utility.getFavoriteMovies(getActivity());
        if (sort_by.equals("favorite")) { //in this case we fetch movies by id's we have in favorite array
            //if the favorite is empty we don't want to show any movie on UI
            movieItems = new ArrayList<MovieItem>();
            if (favoriteMovies != null) {
                FetchFavoritesTask favoriteTask = new FetchFavoritesTask();
                favoriteTask.execute();
            }

        } else {

            if (OnlineUtils.isOnline(getActivity()) == false) {
                Toast.makeText(getActivity(), "no internet connection",
                        Toast.LENGTH_SHORT).show();
            } else {

                FetchMovieTask movieTask = new FetchMovieTask();
                movieTask.execute(sort_by);
            }
        }


    }

    public void UpdateUiAfterLoading(){
        Intent intent = updateDetailIntent(0);
        ((Callback) getActivity()).onFinishLoading(intent);
        if (mPosition != GridView.INVALID_POSITION) {

            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    public void cleardetailView(){
      //  ss:do something here to clear detail view
        Toast.makeText(getActivity(), "no favorite movies,I have to clear detail view",
                Toast.LENGTH_SHORT).show();
    }



    Intent updateDetailIntent(int position){
        Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
        MovieItem movieItem = adapter.getItem(position);
        detailIntent.putExtra("movie_id", movieItem.getId());
        detailIntent.putExtra("title", movieItem.getName());
        detailIntent.putExtra("path", movieItem.getPath());
        detailIntent.putExtra("release_date", movieItem.getReleaseDate());
        detailIntent.putExtra("vote", movieItem.getVote());
        detailIntent.putExtra("overview", movieItem.getOverView());
        detailIntent.putExtra("extra",movieItem.getExtra());
        detailIntent.putExtra("favorite", movieItem.getFavorite());
        return detailIntent;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MovieItem>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<MovieItem> getMovieDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_ID = "id";
            final String MOVIE_LIST = "results";
            final String MOVIE_TITLE = "original_title";
            final String MOVIE_PATH = "poster_path";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RELEASE_DATE = "release_date";
            final String MOVIE_VOTE = "vote_average";
            final String MOVIE_POPULARITY = "popularity";


            JSONObject movieJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_LIST);
            //movie title and movie poster path FOR LOG PURPOSES ONLY
            String[] resultTitles = new String[movieArray.length()];
            String[] resultPaths = new String[movieArray.length()];
            movieItems = new ArrayList<MovieItem>();

            for(int i = 0; i < movieArray.length(); i++) {
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


                MovieItem movieItem = new MovieItem(movie_id,movie_title,movie_path,movie_date,movie_vote,movie_overview,movie_popularity);
                movieItem.setFavorite(Utility.isFavorite(getActivity(),String.valueOf(movie_id)));
                movieItems.add(movieItem);
            }

            return movieItems;

        }


        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {

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
                Log.v(LOG_TAG, movieJsonStr);
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> movieList) {
            if (movieList != null) {
                adapter.clear();
                adapter.add(movieList);
                adapter.notifyDataSetChanged();
                getExtrasInBackground();
                super.onPostExecute(movieList);
            }
        }
    }

    private void getExtrasInBackground() {
        extrasArray = new ArrayList<>();
        //do it for each movie in the movielist
        for(int i=0; i<movieItems.size();i++){
            if(OnlineUtils.isOnline(getActivity()) == false){
                Toast.makeText(getActivity(), "no internet connection",
                        Toast.LENGTH_SHORT).show();
            }else {
                new FetchExtrasTask().execute(String.valueOf(movieItems.get(i).getId()));

            }
        }


    }

    public class FetchExtrasTask extends AsyncTask<String, Void, Extras> {

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
                //   Log.v(LOG_TAG,i+":"+trailers[i].getSource());
            }
            for(int i=0; i<moviesReviewsArray.length();i++){
                JSONObject review = moviesReviewsArray.getJSONObject(i);
                reviews[i] = new Review(review.getString("author"),review.getString("content"));
                //   Log.v(LOG_TAG,i+":"+reviews[i].getBody());
            }

            Extras extras = new Extras(trailers,reviews);

            return extras;

        }

        @Override
        protected Extras doInBackground(String... params) {

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


                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/"+ params[0] +"?";
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
                //update extras array
                extrasArray.add(extras);
                if(extrasArray.size() == movieItems.size()) { //finished loading all extras
                    addExtrasToMovieItems(extrasArray);
                    //fetched all information now update ui
                    UpdateUiAfterLoading();
                }
                super.onPostExecute(extras);
            }
        }
    }




    public class FetchFavoritesTask extends AsyncTask<Void, Void, ArrayList<MovieItem>> {

        private final String LOG_TAG = FetchFavoritesTask.class.getSimpleName();


        private ArrayList<MovieItem> getMovieDataFromDatabase(){

            movieItems = new ArrayList<MovieItem>();
            String movie_title;
            String movie_path;
            String movie_overview;
            double movie_vote;
            String movie_date;
            boolean movie_favorite;
            double movie_popularity;
            int movie_id;
            long movieId;
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
            Cursor cur = getActivity().getContentResolver().query(movieUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);

            while (cur.moveToNext()) {
                //location found
                int movieIdIndex = cur.getColumnIndex(MovieContract.MovieEntry._ID);
                movieId = cur.getLong(movieIdIndex);
                movie_id = Integer.valueOf(cur.getString(COL_MOVIE_KEY));
                movie_title = cur.getString(COL_MOVIE_NAME);
                movie_path= cur.getString(COL_MOVIE_IMAGE_PATH);
                movie_date = cur.getString(COL_MOVIE_RELEASE_DATE);
                movie_vote = cur.getDouble(COL_MOVIE_VOTE);
                movie_overview = cur.getString(COL_MOVIE_OVERVIEW);
                movie_favorite = cur.getInt(COL_MOVIE_FAVORITE)>0;
                movie_popularity = cur.getDouble(COL_MOVIE_POPULARITY);
                Extras extra = getExtrasFromdb(String.valueOf(movie_id));
                movieItems.add(new MovieItem(movie_id,movie_title,movie_path,movie_date,movie_vote,movie_overview,movie_popularity,true,extra));
            }
            return movieItems;

        }

        private Extras getExtrasFromdb(String movie_id) {
            long movieId;
            Uri trailerUri = MovieContract.TrailerEntry.CONTENT_URI;
            Cursor cur = getActivity().getContentResolver().query(trailerUri,
                    TRAILER_COLUMNS,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ?",
                    new String[]{movie_id}, null);

            Trailer[] trailers = new Trailer[cur.getCount()];
            int i=0;
            while (cur.moveToNext()) {
                trailers[i] = new Trailer(cur.getString(COL_TRAILER_NAME),cur.getString(COL_TRAILER_SOURCE));
                i++;
            }
            Uri reviewUri = MovieContract.ReviewEntry.CONTENT_URI;
            cur = getActivity().getContentResolver().query(reviewUri,
                    REVIEW_COLUMNS,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + " = ?",
                    new String[]{movie_id}, null);
            Review[] reviews = new Review[cur.getCount()];
            int j=0;
            while (cur.moveToNext()) {
                reviews[j] = new Review(cur.getString(COL_REVIEW_NAME),cur.getString(COL_REVIEW_BODY));
                j++;
            }
            return new Extras(trailers,reviews);
        }

        @Override
        protected ArrayList<MovieItem> doInBackground(Void... params) {
            return getMovieDataFromDatabase();
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> movieList) {
            if (movieList !=null) {
                adapter.clear();
                adapter.add(movieList);
                adapter.notifyDataSetChanged();
                if(movieList.size() > 0) //in case list is empty we have to clear detail view
                    UpdateUiAfterLoading();
                else
                    cleardetailView();
                super.onPostExecute(movieList);
            }
            super.onPostExecute(movieList);
        }
    }



        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String sort_by = Utility.getSortByCriteria(getActivity());
        String sortOrder = getSortOrder(sort_by);


        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        Cursor cur = getActivity().getContentResolver().query(movieUri,
                null, null, null, sortOrder);
       // cursorAdapter = new ImageCursorAdapter(getActivity(), null, 0);
        adapter = new ImageAdapter(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView_movies);

        if (movieItems != null) {
            adapter.add(movieItems);
        }
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Toast.makeText(getActivity(), "" + adapter.getItem(position).name,
                //         Toast.LENGTH_SHORT).show();
                Intent detailIntent = updateDetailIntent(position);
                ((Callback) getActivity()).onItemSelected(detailIntent);
                mPosition = position;
            }
        });




        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }




    void addExtrasToMovieItems(ArrayList<Extras> extras){
        for(int i=0; i<extras.size();i++){
            movieItems.get(i).setExtras(extras.get(i));
        }
    }



    public String getSortOrder(String sort_by){
        String sortOrder;
        if(sort_by.equals("popularity.desc")){
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        }else if(sort_by.equals("vote_average.desc")){
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE + " DESC";
        }else{//favorite
            sortOrder = MovieContract.MovieEntry.COLUMN_FAVORITE;
        }

        return sortOrder;

    }



    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Intent intent);
        void onFinishLoading(Intent intent);

    }

}
