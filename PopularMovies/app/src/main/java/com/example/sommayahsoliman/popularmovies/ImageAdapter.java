package com.example.sommayahsoliman.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sommayahsoliman on 8/8/15.
 */
public class ImageAdapter extends BaseAdapter {
    private final List<MovieItem> mItems = new ArrayList<MovieItem>();
    private final LayoutInflater mLayoutInflater;
    private final String IMAGE_SIZE = "w342";
    private final String BASE_URL = "http://image.tmdb.org/t/p/";

    public ImageAdapter(Context c) {
        mLayoutInflater = LayoutInflater.from(c);

    }
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MovieItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // create a new ImageView for each item referenced by the Adapter
        View movie_view = convertView;
        ImageView movie_picture;
        TextView movie_name;
        if (convertView == null) {
          movie_view = mLayoutInflater.inflate(R.layout.grid_item_movie,parent, false);
            movie_view.setTag(R.id.grid_item_imageView, movie_view.findViewById(R.id.grid_item_imageView));
            movie_view.setTag(R.id.text, movie_view.findViewById(R.id.text));
        }
        movie_picture = (ImageView) movie_view.getTag(R.id.grid_item_imageView);
        movie_name = (TextView) movie_view.getTag(R.id.text);
        MovieItem item = getItem(position);
        //movie_picture.setImageResource(item.drawableId);
        movie_name.setText(item.getName());
        new DownloadImageTask(movie_picture)
                .execute(BASE_URL + IMAGE_SIZE + item.getPath());
        return movie_view;

    }

    void clear(){
        mItems.clear();
    }

    void add(ArrayList<MovieItem> movies){

        for(MovieItem movie: movies){
            mItems.add(movie);
        }

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


    };



