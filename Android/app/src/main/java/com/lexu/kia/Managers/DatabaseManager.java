package com.lexu.kia.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class DatabaseManager {

    static final float EARTH_RADIUS = 6371;  //Earth radius in km

    private SQLiteDatabase mDatabase = null;
    private String mQuery = null;
    private ArrayList<ContentValues> mItems = null;

    private DatabaseCallback mCallback = null;

    private DatabaseManager(Context context) {
        mDatabase = context.openOrCreateDatabase(
                DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME,
                Context.MODE_PRIVATE,
                null
        );
    }

    final DatabaseManager setQuery(String query) {
        mQuery = query;
        return this;
    }

    final DatabaseManager setContentValues(ArrayList<ContentValues> contentValues) {
        mItems = contentValues;
        return this;
    }

    final DatabaseManager setCallback(DatabaseCallback callback) {
        mCallback = callback;
        return this;
    }

    final synchronized void get() {
        if(mQuery.length() == 0) {
            mCallback.onFailure(
                    new DatabaseCallback.ResponseData.Builder<String>()
                            .data("There was no body inside the query")
                            .message("Invalid request")
                            .status(DatabaseCallback.ResponseStatus.FAILURE)
                            .build()
            );
            return;
        }

        Cursor cursor = mDatabase.rawQuery(mQuery, null);

        //TODO: send Cursor for processing
        cursor.close();
    }

    final synchronized void post() {
        if(mItems.size() == 0) {
            mCallback.onFailure(
                    new DatabaseCallback.ResponseData.Builder<String>()
                            .data("There were no items sent for processing")
                            .message("Invalid request")
                            .status(DatabaseCallback.ResponseStatus.FAILURE)
                            .build()
            );
            return;
        }

        for(ContentValues cv: mItems) {
            long id = mDatabase.insert(
                    DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME,
                    null,
                    cv
            );

            if(id == -1) {
                mCallback.onFailure(
                        new DatabaseCallback.ResponseData.Builder<String>()
                                .data("The record could not be inserted; error: " + id)
                                .message("Error inserting record in database")
                                .status(DatabaseCallback.ResponseStatus.FAILURE)
                                .build()
                );
                return;
            }
        }
    }

    final void close() {
        if(mDatabase != null) {
            mDatabase.close();
        }
    }

    public static final class Builder {
        private DatabaseManager databaseManager = null;

        private float latitude = 0;
        private float longitude = 0;
        private float range = 0;

        private String query = null;
        private ArrayList<ContentValues> contentValues = null;

        /**
         * Set the context used to open / create the database connection
         * @param context that is to be used for opening / creating the database connection
         * @return the Builder instance used for database instantiation
         */
        public final synchronized Builder with(Context context) {
            if(this.databaseManager == null) {
                this.databaseManager = new DatabaseManager(context);
            }

            return this;
        }

        /**
         * Set user's current position
         * @param lat of the user at that moment in time
         * @param lng of the user at that moment in time
         * @param range set by the user
         * @return the Builder instance used for database instantiation
         */
        public final Builder location(float lat, float lng, float range) {
            this.latitude = lat;
            this.longitude = lng;
            this.range = range;
            return this;
        }

        /**
         * Usage of this method is optional; It should be used for
         * initiating the DB, clearing it of data, or retrieving data.
         *
         * Generate the required request type
         * @see DatabaseUtils.DatabaseRequestType
         *
         * @param requestType to generate the required query content
         * @return the Builder instance used for database instantiation
         */
        public final Builder option(DatabaseUtils.DatabaseRequestType requestType) {
            switch (requestType) {   //TODO: Replace query init with generation of query according to requestType
                case CREATE_TABLE:
                    this.query = DatabaseQueryBuilder.createTable();
                    break;

                case DROP_TABLE:
                    this.query = DatabaseQueryBuilder.deleteAll();
                    break;

                case GET_LOCATIONS:
                    this.query = DatabaseQueryBuilder.selectAll(this.latitude, this.longitude, this.range);
                    break;
            }

            return this;
        }

        /**
         * Sets the callback to be used once the processing of the query has been completed
         * @return the Builder instance used for database instantiation
         */
        public final Builder callback() {
            //TODO: create the actual Callback type and set it for further use
            return this;
        }

        public final synchronized void build() {
            if(this.contentValues.size() == 0) {
                this.databaseManager
                        .setQuery(this.query)
                        //.setCallback(this.callback)
                        .get();
            } else {
                this.databaseManager
                        .setContentValues(this.contentValues)
                        //.setCallback(this.callback)
                        .post();
            }
        }

        public final synchronized void close() {
            if(this.databaseManager != null) {
                this.databaseManager.close();
            }
        }
    }
}

class DatabaseUtils {
    enum DatabaseRequestType {
        DROP_TABLE, GET_LOCATIONS, PUT_LOCATION, CREATE_TABLE
    }

    static final class FeedReaderContract {
        private FeedReaderContract() {
        }

        static class FeedEntry implements BaseColumns {
            static final String TABLE_NAME = "KIA_LOCATIONS";
            static final String COLUMN_NAME_TITLE = "KIA_TITLE";
            static final String COLUMN_NAME_NOTE = "KIA_NOTE";
            static final String COLUMN_NAME_LATITUDE = "KIA_LATITUDE";
            static final String COLUMN_NAME_LONGITUDE = "KIA_LONGITUDE";
            static final String COLUMN_NAME_RATING = "KIA_RATING";
            static final String COLUMN_NAME_IMAGES = "KIA_IMAGES";
            static final String DISTANCE = "D";
        }
    }
}

final class DatabaseQueryBuilder {
    static String createTable() {
        return "CREATE TABLE " +
                DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
                DatabaseUtils.FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY, " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " TEXT, " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_NOTE + " TEXT, " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_RATING + " DOUBLE(2, 1), " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE + " DOUBLE(9, 6), " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE + " DOUBLE(9, 6), " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_IMAGES + " TEXT)";
    }

    static String deleteAll() {
        return "DROP TABLE IF EXISTS " + DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME;
    }

    static String selectAll(float lat, float lng, float rad) {
        return "SELECT " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + ", " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_NOTE + ", " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_RATING + ", " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE + ", " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE + ", " +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_IMAGES + ", " +
                distance(lat, lng) + " AS " + DatabaseUtils.FeedReaderContract.FeedEntry.DISTANCE + " " +
                "FROM " +
                DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME +
                "WHERE " +
                DatabaseUtils.FeedReaderContract.FeedEntry.DISTANCE +
                " < " + rad + " " +
                "ORDER BY " + DatabaseUtils.FeedReaderContract.FeedEntry.DISTANCE + " ASC";
    }

    private static String distance(float userLat, float userLon) {
        return "acos(sin(" +
                userLat +
                ")*sin(radians(" +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE +
                ")) + cos(" +
                userLat +
                ")*cos(radians(" +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE +
                "))*cos(radians(" +
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE +
                ")-" +
                userLon +
                ")) * " + DatabaseManager.EARTH_RADIUS;
    }
}