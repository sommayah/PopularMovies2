package com.example.sommayahsoliman.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by sommayahsoliman on 9/15/15.
 */
public class MovieProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int TRAILER_FOR_MOVIE = 201;
    public static final int REVIEW_FOR_MOVIE = 301;
    public static final int TRAILER = 200;
    public static final int REVIEW = 300;

    private static final SQLiteQueryBuilder sTrailersForMovieQueryBuilder;
    private static final SQLiteQueryBuilder sReviewsForMovieQueryBuilder;
    private static final SQLiteQueryBuilder sMoviesForMovieQueryBuilder;

    static{
        sTrailersForMovieQueryBuilder = new SQLiteQueryBuilder();


        //This is an inner join which looks like
        //trailer INNER JOIN movie ON trailer.movie_id = movie._id
        sTrailersForMovieQueryBuilder.setTables(
                MovieContract.TrailerEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.TrailerEntry.TABLE_NAME +
                        "." + MovieContract.TrailerEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);

    }
    static{
        sReviewsForMovieQueryBuilder = new SQLiteQueryBuilder();


        //This is an inner join which looks like
        //trailer INNER JOIN movie ON trailer.movie_id = movie._id
        sReviewsForMovieQueryBuilder.setTables(
                MovieContract.ReviewEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.ReviewEntry.TABLE_NAME +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_KEY +
                        " = " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);

    }
    static{
        sMoviesForMovieQueryBuilder = new SQLiteQueryBuilder();


        //This is an inner join which looks like
        //trailer INNER JOIN movie ON trailer.movie_id = movie._id
        sMoviesForMovieQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME);

    }

    //movie.movie_id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " = ? ";

    private static final String sTrailerIdSelection =
            MovieContract.TrailerEntry.TABLE_NAME+
                    "." + MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ? ";

    //get all trailers for a movie with movie_id
    private Cursor getTrailerByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.MovieEntry.getMovieIdFromUri(uri);


        String[] selectionArgs;
        String selection;
        selection = sTrailerIdSelection;
        selectionArgs = new String[]{movie_id};

        return sTrailersForMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //get all reviews for a movie with movie_id
    private Cursor getReviewsByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.MovieEntry.getMovieIdFromUri(uri);


        String[] selectionArgs;
        String selection;
        selection = sMovieIdSelection;
        selectionArgs = new String[]{movie_id};

        return sReviewsForMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder){
        String movie_id = MovieContract.MovieEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        String selection;
        selection = sMovieIdSelection;
        selectionArgs = new String[]{movie_id};

        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }



    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    public static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        uriMatcher.addURI(authority,MovieContract.PATH_MOVIE , MOVIE);
        uriMatcher.addURI(authority,MovieContract.PATH_MOVIE+ "/*", MOVIE_WITH_ID);
        uriMatcher.addURI(authority,MovieContract.PATH_TRAILER + "/*", TRAILER_FOR_MOVIE);
        uriMatcher.addURI(authority,MovieContract.PATH_REVIEW + "/*", REVIEW_FOR_MOVIE);
        uriMatcher.addURI(authority,MovieContract.PATH_TRAILER , TRAILER);
        uriMatcher.addURI(authority,MovieContract.PATH_REVIEW , REVIEW);



        // 3) Return the new matcher!
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    public void clearDatabase(Context context) { //used because I edited one of the columns after creating db

        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case REVIEW_FOR_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case TRAILER_FOR_MOVIE:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "trailer/*"
            case TRAILER_FOR_MOVIE:
            {
                retCursor = getTrailerByMovieId(uri, projection, sortOrder);
                break;
            }
            // "review/*"
            case REVIEW_FOR_MOVIE: {
                retCursor = getReviewsByMovieId(uri, projection, sortOrder);
                break;
            }
            case MOVIE_WITH_ID:{
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }



            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TRAILER:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case REVIEW:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME,null,values);
                if(_id > 0)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                break;

            }
            case REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME,null,values);
                if(_id > 0)
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        if(null == selection) selection = "1";
        switch (match){
            case MOVIE:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if(rowsDeleted !=0)
            getContext().getContentResolver().notifyChange(uri, null);
        // Student: return the actual rows deleted
        return rowsDeleted;
    }



    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int row;
        switch (match){
            case MOVIE:
                row = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case TRAILER:
                row = db.update(MovieContract.TrailerEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case REVIEW:
                row = db.update(MovieContract.ReviewEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("unknown uri:" + uri);


        }
        if(row !=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
