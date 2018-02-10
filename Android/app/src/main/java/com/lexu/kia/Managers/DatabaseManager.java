package com.lexu.kia.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.lexu.kia.Model.LocationBuilder;
import com.lexu.kia.Model.LocationEntry;

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
        if (mQuery.length() == 0) {
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
        int COLUMN_INDEX_ID = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry._ID);
        int COLUMN_INDEX_TITLE = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE);
        int COLUMN_INDEX_COMMENTS = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_NOTE);
        int COLUMN_INDEX_RATING = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_RATING);
        int COLUMN_INDEX_LATITUDE = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE);
        int COLUMN_INDEX_LONGITUDE = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE);
        int COLUMN_INDEX_IMAGES = cursor.getColumnIndex(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_IMAGES);

        cursor.moveToFirst();
        do {
            long _ID = cursor.getLong(COLUMN_INDEX_ID);
            String title = cursor.getString(COLUMN_INDEX_TITLE);
            String notes = cursor.getString(COLUMN_INDEX_COMMENTS);
            double rating = cursor.getDouble(COLUMN_INDEX_RATING);
            float lat = cursor.getFloat(COLUMN_INDEX_LATITUDE);
            float lng = cursor.getFloat(COLUMN_INDEX_LONGITUDE);
            String images = cursor.getString(COLUMN_INDEX_IMAGES);

            if(_ID < 0) {
                mCallback.onFailure(
                        new DatabaseCallback.ResponseData.Builder<String>()
                        .data("An error occurred")
                        .message("Could not retrieve data from database")
                        .status(DatabaseCallback.ResponseStatus.FAILURE)
                        .build()
                );
            } else {
                mCallback.onComplete(
                        new DatabaseCallback.ResponseData.Builder<LocationEntry>()
                        .data(
                                new LocationBuilder()
                                        .with(_ID)
                                        .with(title, notes)
                                        .with(rating)
                                        .with(lat, lng)
                                        .with(images)
                                        .build()
                        )
                        .status(DatabaseCallback.ResponseStatus.SUCCESS)
                        .message("Success")
                        .build()
                );
            }
        } while (cursor.moveToNext());
        cursor.close();
    }

    final synchronized void post(boolean isUpdate) {
        if (mItems.size() == 0) {
            mCallback.onFailure(
                    new DatabaseCallback.ResponseData.Builder<String>()
                            .data("There were no items sent for processing")
                            .message("Invalid request")
                            .status(DatabaseCallback.ResponseStatus.FAILURE)
                            .build()
            );
            return;
        }

        for (ContentValues cv : mItems) {
            long id = isUpdate ?
                    mDatabase.update(
                            DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME,
                            cv,
                            DatabaseUtils.FeedReaderContract.FeedEntry._ID + " = ?",
                            new String[]{String.valueOf(cv.getAsFloat(DatabaseUtils.FeedReaderContract.FeedEntry._ID))}
                    ) :
                    mDatabase.insert(
                            DatabaseUtils.FeedReaderContract.FeedEntry.TABLE_NAME,
                            null,
                            cv
                    );

            if (id == -1 && !isUpdate) {
                mCallback.onFailure(
                        new DatabaseCallback.ResponseData.Builder<String>()
                                .data("The record could not be inserted; error: " + id)
                                .message("Error inserting record in database")
                                .status(DatabaseCallback.ResponseStatus.FAILURE)
                                .build()
                );
                return;
            } else if (id == 0 && isUpdate) {
                mCallback.onFailure(
                        new DatabaseCallback.ResponseData.Builder<String>()
                                .data("The record could not be updated")
                                .message("Error updating record")
                                .status(DatabaseCallback.ResponseStatus.FAILURE)
                                .build()
                );
                return;
            }

            mCallback.onComplete(
                    new DatabaseCallback.ResponseData.Builder<Float>()
                            .message(isUpdate ? "Record updated successfully" : "Record inserted successfully")
                            .data((float) id)
                            .status(DatabaseCallback.ResponseStatus.SUCCESS)
                            .build()
            );
        }
    }

    final void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public static final class Builder {
        private DatabaseManager databaseManager = null;

        private String search = null;
        private float latitude = 0;
        private float longitude = 0;
        private float range = 0;
        private boolean isUpdate = false;

        private String query = null;
        private ArrayList<ContentValues> contentValues = null;
        private DatabaseCallback callback = null;

        /**
         * Set the context used to open / create the database connection
         *
         * @param context that is to be used for opening / creating the database connection
         * @return the Builder instance used for database instantiation
         */
        public final synchronized Builder with(Context context) {
            if (this.databaseManager == null) {
                this.databaseManager = new DatabaseManager(context);
            }

            return this;
        }

        /**
         * Set the new entries / entries to update
         * @param entries that need to be inserted / updated
         * @return the Builder instance used for database instantiation
         */
        public final synchronized Builder with(ArrayList<LocationEntry> entries) {
            this.contentValues = new ArrayList<ContentValues>();

            for(LocationEntry e: entries) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry._ID, e.getId());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, e.getName());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_NOTE, e.getComments());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_RATING, e.getRating());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE, e.getLatitude());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE, e.getLongitude());
                cv.put(DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_IMAGES, e.getImages());

                this.contentValues.add(cv);
            }

            return this;
        }

        /**
         * Set user's current position
         *
         * @param lat   of the user at that moment in time
         * @param lng   of the user at that moment in time
         * @param range set by the user
         * @return the Builder instance used for database instantiation
         */
        public final Builder with(float lat, float lng, float range) {
            this.latitude = lat;
            this.longitude = lng;
            this.range = range;
            return this;
        }

        /**
         * Set the search criteria
         *
         * @param name of the location to search for
         * @return the Builder instance used for database instantiation
         */
        public final Builder with(String name) {
            this.search = name;
            return this;
        }

        /**
         * Usage of this method is optional; It should be used for
         * initiating the DB, clearing it of data, or retrieving data.
         * <p>
         * Generate the required request type
         *
         * @param requestType to generate the required query content
         * @return the Builder instance used for database instantiation
         * @see DatabaseUtils.DatabaseRequestType
         */
        public final Builder with(DatabaseUtils.DatabaseRequestType requestType) {
            switch (requestType) {
                case CREATE_TABLE:
                    this.query = DatabaseQueryBuilder.createTable();
                    break;

                case DROP_TABLE:
                    this.query = DatabaseQueryBuilder.deleteAll();
                    break;

                case GET_LOCATIONS:
                    this.query = DatabaseQueryBuilder.selectAll(this.latitude, this.longitude, this.range);
                    break;

                case GET_BY_NAME:
                    this.query = DatabaseQueryBuilder.selectByName(this.latitude, this.longitude, this.range, this.search);
                    break;
            }

            return this;
        }

        /**
         * Sets the callback to be used once the processing of the query has been completed
         *
         * @return the Builder instance used for database instantiation
         */
        public final Builder callback(DatabaseCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Sets the flag for updating / inserting new records in the database
         *
         * @param isUpdate operation to be done on the database
         * @return the Builder instance used for database instantiation
         */
        public final Builder with(boolean isUpdate) {
            this.isUpdate = isUpdate;
            return this;
        }

        /**
         * Launches the request to the database
         */
        public final synchronized void build() {
            if (this.contentValues.size() == 0) {
                this.databaseManager
                        .setQuery(this.query)
                        .setCallback(this.callback)
                        .get();
            } else {
                this.databaseManager
                        .setContentValues(this.contentValues)
                        .setCallback(this.callback)
                        .post(this.isUpdate);
            }
        }

        /**
         * Closes the database on instance destruction
         * @throws Throwable
         */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            this.close();
        }

        /**
         * Used for closing the database and resetting all data used by the manager
         */
        final synchronized void close() {
            if (this.databaseManager != null) {
                this.databaseManager.close();
                this.search = null;
                this.callback = null;
                this.longitude = 0;
                this.latitude = 0;
                this.query = null;
                this.range = 0;
                this.contentValues = null;
            }
        }
    }
}

final class DatabaseUtils {
    enum DatabaseRequestType {
        DROP_TABLE, GET_LOCATIONS, GET_BY_NAME, CREATE_TABLE
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
                DatabaseUtils.FeedReaderContract.FeedEntry._ID + ", " +
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

    static String selectByName(float lat, float lng, float rad, String search) {
        return "SELECT " +
                DatabaseUtils.FeedReaderContract.FeedEntry._ID + ", " +
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
                DatabaseUtils.FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE " +
                "%" + search + "% AND " +
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