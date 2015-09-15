package com.example.sommayahsoliman.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
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
    public LinearLayout mLayout;
    private boolean favorite;

    TextView mTextView;
    TextView mDateTextView;
    TextView mVoteTextView;
    TextView mOverviewTextView;
    ImageView mImageView;
    Button mFavorite_btn;
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
        outState.putParcelable(DETAIL_INTENT, mIntent);
        super.onSaveInstanceState(outState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        if(arguments != null){
            mIntent = arguments.getParcelable(DetailActivityFragment.DETAIL_INTENT);
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

        updateUI(mIntent);
        return rootView;
    }

    void updateUI(Intent intent){
        if(intent != null && intent.hasExtra("title")){
            name = intent.getStringExtra("title");
            path = intent.getStringExtra("path");
            release_date = intent.getStringExtra("release_date");
            vote = intent.getDoubleExtra("vote", 0);
            overview = intent.getStringExtra("overview");
            movie_id = intent.getIntExtra("movie_id", 0);
            extras = intent.getParcelableExtra("extra");
            favorite = intent.getBooleanExtra("favorite", false);
            mTextView.setText(name);
            mDateTextView.setText(RELEASE_DATE+release_date);
            mVoteTextView.setText(VOTE + String.valueOf(vote));
            mOverviewTextView.setText(overview);
            if(favorite == true){
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
            }else{
                mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            }

            if(OnlineUtils.isOnline(getActivity()) == false){
                Toast.makeText(getActivity(), "no internet connection",
                        Toast.LENGTH_SHORT).show();
            }else {
                new DownloadImageTask(mImageView)
                        .execute(BASE_URL + IMAGE_SIZE + path);
            }

            if(extras ==null) { //fetch it if it is not saved from before
                if (OnlineUtils.isOnline(getActivity()) == false) {
                    Toast.makeText(getActivity(), "no internet connection",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //new FetchExtrasTask().execute(movie_id);

                }
            }else{
                addTrailers(extras);
                addReviews(extras);
            }

        }
        mIntent = intent;
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
            mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
        } else {
            favoriteMovies.remove(id_string); //if user press twice on favorite the movie get removed from favorites
            mFavorite_btn.setBackgroundResource(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
        }
        editor.putStringSet("favorite", favoriteMovies);
        editor.commit();
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


