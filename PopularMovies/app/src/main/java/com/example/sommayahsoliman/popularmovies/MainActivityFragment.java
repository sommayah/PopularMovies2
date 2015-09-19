package com.example.sommayahsoliman.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.sommayahsoliman.popularmovies.data.MovieContract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    ArrayList<MovieItem> movieItems;
    ArrayList<Extras> extrasArray;
    ImageAdapter adapter;
    Set<String> favoriteMovies;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private GridView mGridView;

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
            MovieContract.MovieEntry.COLUMN_FAVORITE,

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                updateMovies();
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);

        if(savedInstanceState != null && savedInstanceState.containsKey("movies")) {
             movieItems = savedInstanceState.getParcelableArrayList("movies");
        }else{
            //movieItems = new ArrayList<MovieItem>();
            updateMovies();
        }
        //to listen to setting changes and fetch new data when changed


    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
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

        String sort_by = Utility.getSortByCriteria(getActivity());
        favoriteMovies = Utility.getFavoriteMovies(getActivity());


        if(OnlineUtils.isOnline(getActivity()) == false){
            Toast.makeText(getActivity(), "no internet connection",
                    Toast.LENGTH_SHORT).show();
        }else {
            if(sort_by.equals("favorite"))
            { //in this case we fetch movies by id's we have in favorite array
                //if the favorite is empty we don't want to show any movie on UI
                movieItems = new ArrayList<MovieItem>();

                if (favoriteMovies != null) {
                    for (Iterator<String> it = favoriteMovies.iterator(); it.hasNext(); ) {
                        String movie_id = it.next();
                        FetchFavoritesTask favoriteTask = new FetchFavoritesTask(getActivity());
                        favoriteTask.execute(movie_id);
                    }
                }
            }else {
                FetchMovieTask movieTask = new FetchMovieTask(getActivity());
                movieTask.execute(sort_by);
            }
        }

        getLoaderManager().restartLoader(MY_LOADER_ID,null,this);


    }

 /*   public void UpdateUiAfterLoading(){
        Intent intent = updateDetailIntent(0);
        ((Callback) getActivity()).onFinishLoading(intent);
        if (mPosition != GridView.INVALID_POSITION) {

            mGridView.smoothScrollToPosition(mPosition);
        }
    }
*/



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String sort_by = Utility.getSortByCriteria(getActivity());
        String sortOrder = getSortOrder(sort_by);


        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        Cursor cur = getActivity().getContentResolver().query(movieUri,
                null, null, null, sortOrder);
        adapter = new ImageAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        mGridView.setAdapter(adapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(MovieContract.MovieEntry.buildMovieWithIdUri(
                            cursor.getString(COL_MOVIE_KEY)));

                }
                mPosition = position;
//                Intent detailIntent = updateDetailIntent(position);
//                ((Callback) getActivity()).onItemSelected(detailIntent);
//                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

  /*  Intent updateDetailIntent(int position){
        Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
        MovieItem movieItem = adapter.getItem(position);
        detailIntent.putExtra("movie_id", movieItem.getId());
        detailIntent.putExtra("title", movieItem.getName());
        detailIntent.putExtra("path", movieItem.getPath());
        detailIntent.putExtra("release_date", movieItem.getReleaseDate());
        detailIntent.putExtra("vote", movieItem.getVote());
        detailIntent.putExtra("popularity", movieItem.getPopularity());
        detailIntent.putExtra("overview", movieItem.getOverView());
        detailIntent.putExtra("extra",movieItem.getExtra());
        detailIntent.putExtra("favorite",movieItem.getFavorite());
        return detailIntent;
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        String sort_by = Utility.getSortByCriteria(getActivity());
        String sortOrder = getSortOrder(sort_by);
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;



        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        Loader<Cursor> loader = new CursorLoader(getActivity(), movieUri,
                MOVIE_COLUMNS, null, null,
                MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " ASC");

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
// old cursor once we return.)
        adapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
        void onItemSelected(Uri dateUri);
        void onFinishLoading(Intent intent);
    }

}
