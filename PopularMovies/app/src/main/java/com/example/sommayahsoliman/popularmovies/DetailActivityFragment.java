package com.example.sommayahsoliman.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sommayahsoliman.popularmovies.data.MovieContract;

import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String IMAGE_SIZE = "w342";
    private final String RELEASE_DATE = "Release Date: ";
    private final String VOTE = "Vote: ";
    private final String BASE_URL = "http://image.tmdb.org/t/p/";
    static final String DETAIL_INTENT = "detail_movie";
    private String name;
    private String path;
    private String release_date;
    private double vote;
    private String overview;
    private String movie_id;
    private Extras extras; //this includes trailers and reviews
    public LinearLayout mLayout;
    private Intent mIntent;
    private boolean favorite;
    private double popularity;

    TextView mTextView;
    TextView mDateTextView;
    TextView mVoteTextView;
    TextView mOverviewTextView;
    ImageView mImageView;
    Button mFavorite_btn;
    private Uri mUri;
    private Uri mTUri;
    private MovieItem mMovieItem;

    static final String DETAIL_URI = "URI";
    static final String TRAILER_URI ="TURI";

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String[] DETAIL_COLUMNS = {
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
        mIntent = new Intent();
        if(savedInstanceState != null && savedInstanceState.containsKey(DETAIL_INTENT)) {
            mIntent = savedInstanceState.getParcelable(DETAIL_INTENT);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DETAIL_URI, mUri);
        outState.putParcelable(DETAIL_INTENT, mIntent);
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI); //incase of favorites
            mIntent = arguments.getParcelable(DETAIL_INTENT); //in case of popularity or vote average
        }
        mLayout = (LinearLayout)rootView.findViewById(R.id.outerLayout);
        mTextView = (TextView)rootView.findViewById(R.id.textViewTitle);
        mDateTextView = (TextView)rootView.findViewById(R.id.textViewDate);
        mVoteTextView = (TextView)rootView.findViewById(R.id.textViewVote);
        mOverviewTextView = (TextView)rootView.findViewById(R.id.textViewOverView);
        mImageView = (ImageView)rootView.findViewById(R.id.imageView);
        mFavorite_btn = (Button) rootView.findViewById(R.id.favorite_btn);
        mFavorite_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavoriteClick();
            }
        });
        if (mIntent != null)
            updateUI(mIntent);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
       // getLoaderManager().initLoader(TRAILER_LOADER,null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void updateUI(Uri data){
        mUri = data;
        if (null != mUri) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }

    }

    void updateUI(Intent intent){ //incase of vote average and popularity
        if(intent != null && intent.hasExtra("title")){
            name = intent.getStringExtra("title");
            path = intent.getStringExtra("path");
            release_date = intent.getStringExtra("release_date");
            vote = intent.getDoubleExtra("vote", 0);
            overview = intent.getStringExtra("overview");
            movie_id = String.valueOf(intent.getIntExtra("movie_id", 0));
            extras = intent.getParcelableExtra("extra");
            favorite = intent.getBooleanExtra("favorite", false);
            popularity = intent.getDoubleExtra("popularity",0);
            mTextView.setText(name);
            mDateTextView.setText(RELEASE_DATE+release_date);
            mVoteTextView.setText(VOTE + String.valueOf(vote));
            mOverviewTextView.setText(overview);
            if(favorite == true){
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
            }else{
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            }

            Utility.setImageResource(getActivity(),mImageView,path);

            if(extras ==null) { //fetch it if it is not saved from before
               /* if (OnlineUtils.isOnline(getActivity()) == false) {
                    Toast.makeText(getActivity(), "no internet connection",
                            Toast.LENGTH_SHORT).show();
                } else {
                    new FetchExtrasTask().execute(movie_id);

                }*/
            }else{
                addTrailers(extras);
                addReviews(extras);
            }

        }
        mIntent = intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

                if (null != mUri) {
                    // Now create and return a CursorLoader that will take care of
                    // creating a Cursor for the data being displayed.
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS,
                            null,
                            null,
                            null
                    );
                }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            movie_id = data.getString(COL_MOVIE_KEY);
            name = data.getString(COL_MOVIE_NAME);
            path = data.getString(COL_MOVIE_IMAGE_PATH);
            release_date = data.getString(COL_MOVIE_RELEASE_DATE);
            vote = data.getDouble(COL_MOVIE_VOTE);
            overview = data.getString(COL_MOVIE_OVERVIEW);
            int dataFavorite = data.getInt(COL_MOVIE_FAVORITE);
            favorite = data.getInt(COL_MOVIE_FAVORITE)>0;
            mTextView.setText(name);
            mDateTextView.setText(RELEASE_DATE+release_date);
            mVoteTextView.setText(VOTE + String.valueOf(vote));
            mOverviewTextView.setText(overview);
            // Use placeholder Image
            if(favorite == true){
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
            }else{
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            }
            Utility.setImageResource(getActivity(),mImageView,path);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
            addMovieTodb(movie_id,name,path,release_date,vote,overview,popularity,extras);
        } else {
            favoriteMovies.remove(id_string); //if user press twice on favorite the movie get removed from favorites
            mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            removeMovieFromdb(movie_id);
        }
        editor.putStringSet("favorite", favoriteMovies);
        editor.commit();
    }

    private void removeMovieFromdb(String movie_id) {
        long movieId;
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        movieId = getActivity().getContentResolver().delete(movieUri,MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " = ?"
                ,new String[]{movie_id});


    }

    public void addMovieTodb(String movie_id,String name, String path, String releaseDate, double vote,
                         String overView, double popularity,Extras extra) {
        // Students: First, check if the trailer exists in the db
        // If it exists, return the current ID
        long movieId;
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        Cursor cur = getActivity().getContentResolver().query(movieUri,
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
            Uri uri = getActivity().getContentResolver().insert(movieUri, movieValues);

            movieId = ContentUris.parseId(uri);
        }
        //addtrailer and reviews
        for(int i=0; i<extra.getTrailersNum(); i++){
            Trailer t = extra.getTrailerAtIndex(i);
            Utility.addTrailer(movie_id,t.getTitle(),t.getSource(),getActivity());
        }
        for(int i=0; i<extra.getReviewsNum(); i++){
            Review r = extra.getReviewAtIndex(i);
            Utility.addReview(movie_id,r.getAuthor(),r.getBody(),getActivity());
        }

    }





    public void addTrailers(final Extras extra){
        int numTrailers = extra.getTrailersNum();
        if(numTrailers >0){
            //add trailers title
            addLine(); //add a divider
            TextView title = new TextView(getActivity());
            title.setText("Trailers:");
            title.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Large);
            mLayout.addView(title);
        }
        for(int i=0; i<numTrailers;i++){
            LinearLayout trailerLayout =new LinearLayout(getActivity());
            trailerLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView trailerName = new TextView(getActivity());
            trailerName.setText(extra.getTrailerAtIndex(i).getTitle());
            trailerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 4f;
            trailerName.setLayoutParams(params);
            trailerLayout.addView(trailerName);

            Button btn = new Button(getActivity());
            btn.setTag(i);
            btn.setText("Go!");
            LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            paramsBtn.weight = 1f;
            btn.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            btn.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    String source = extra.getTrailerAtIndex((int) v.getTag()).getSource();
                    watchYoutubeVideo(source);

                }
            });
            trailerLayout.addView(btn);
            mLayout.addView(trailerLayout); //add each individual trailer

        }

    }

    public void addReviews(final Extras extra){
        int numReviews = extra.getReviewsNum();
        if(numReviews >0){
            //add reviews title
            addLine(); //add a divider
            TextView title = new TextView(getActivity());
            title.setText("Reviews:");
            title.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Large);
            mLayout.addView(title);
        }
        for(int i=0; i<numReviews;i++){
            LinearLayout reviewLayout =new LinearLayout(getActivity());
            reviewLayout.setOrientation(LinearLayout.VERTICAL);

            TextView authorName = new TextView(getActivity());
            authorName.setText(extra.getReviewAtIndex(i).getAuthor()+":");
            authorName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            authorName.setTextColor(Color.LTGRAY);
            reviewLayout.addView(authorName);

            TextView reviewBody = new TextView(getActivity());
            reviewBody.setText(extra.getReviewAtIndex(i).getBody());
           // reviewBody.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
           // reviewBody.setTextColor(Color.DKGRAY);
            reviewBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 15, 0, 15);
            reviewBody.setLayoutParams(params);

            reviewLayout.addView(reviewBody);


            mLayout.addView(reviewLayout); //add each individual trailer

        }

    }
    public void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            startActivity(intent);
        }
    }

    public void addLine(){
        View v = new View(getActivity());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));

        mLayout.addView(v);
    }

}


