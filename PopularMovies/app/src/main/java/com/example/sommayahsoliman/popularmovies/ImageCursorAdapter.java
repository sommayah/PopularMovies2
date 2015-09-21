package com.example.sommayahsoliman.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sommayahsoliman on 8/8/15.
 */
public class ImageCursorAdapter extends CursorAdapter {
    public ImageCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

  //  private final List<MovieItem> mItems = new ArrayList<MovieItem>();
   // private final LayoutInflater mLayoutInflater;



    @Override
    public long getItemId(int position) {

        return 0;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // create a new ImageView for each item referenced by the Adapter
//        View movie_view = convertView;
//        ImageView movie_picture;
//        TextView movie_name;
//        if (convertView == null) {
//          movie_view = mLayoutInflater.inflate(R.layout.grid_item_movie,parent, false);
//            movie_view.setTag(R.id.grid_item_imageView, movie_view.findViewById(R.id.grid_item_imageView));
//            movie_view.setTag(R.id.text, movie_view.findViewById(R.id.text));
//        }
//        movie_picture = (ImageView) movie_view.getTag(R.id.grid_item_imageView);
//        movie_name = (TextView) movie_view.getTag(R.id.text);
//        MovieItem item = getItem(position);
//        //movie_picture.setImageResource(item.drawableId);
//        movie_name.setText(item.getName());
//        new DownloadImageTask(movie_picture)
//                .execute(BASE_URL + IMAGE_SIZE + item.getPath());
//        return movie_view;
//
//    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        layoutId = R.layout.grid_item_movie;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);



        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read weather icon ID from cursor
        String imageId = cursor.getString(MainActivityFragment.COL_MOVIE_IMAGE_PATH);
        Utility.setImageResource(mContext,viewHolder.movie_picture,imageId);

        String imageName = cursor.getString(MainActivityFragment.COL_MOVIE_NAME);
        viewHolder.movie_name.setText(imageName);

    }




    public static class ViewHolder {
        public final ImageView movie_picture;
        public final TextView movie_name;

        public ViewHolder(View view) {
            movie_picture = (ImageView) view.findViewById(R.id.grid_item_imageView);
            movie_name = (TextView) view.findViewById(R.id.text);
        }
    }


    }



