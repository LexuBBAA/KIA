package com.lexu.kia.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LocationEntry {

    private static final String TAG = "LocationEntry";

    private long m_ID = -1;
    private String mName = null;
    private String mComments = null;
    private double mRating = -1;
    private float mLatitude = Float.MIN_VALUE;
    private float mLongitude = Float.MIN_VALUE;
    private String mImages = null;

    private  LocationEntry(@NonNull String name, @Nullable String comments, double rating, float lat, float lng, @Nullable String images) {
        mName = name;
        mComments = comments;
        mRating = rating;
        mLatitude = lat;
        mLongitude = lng;
        mImages = images;
    }

    private LocationEntry(long id, @NonNull String name, @Nullable String comments, double rating, float lat, float lng, @Nullable String images) {
        m_ID = id;
        mName = name;
        mComments = comments;
        mRating = rating;
        mLatitude = lat;
        mLongitude = lng;
        mImages = images;
    }

    public long getId() {
        return m_ID;
    }

    public String getName() {
        return mName;
    }

    public String getComments() {
        return mComments;
    }

    public double getRating() {
        return mRating;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public String getImages() {
        return mImages;
    }

    protected void setId(long id) {
        m_ID = id;
    }

    void setName(String name) {
        mName = name;
    }

    void setComments(String comments) {
        mComments = comments;
    }

    void setRating(double rating) {
        mRating = rating;
    }

    void setLatitude(float latitude) {
        mLatitude = latitude;
    }

    void setLongitude(float longitude) {
        mLongitude = longitude;
    }

    protected static abstract class AbstractBuilder<T> {
        private static final String TAG = "AbstractBuilder";

        long id = -1;
        String name = null;
        String comm = null;
        double rating = -1;
        float lat = Float.MIN_VALUE;
        float lng = Float.MIN_VALUE;
        String images = null;

        AbstractBuilder() {
        }

        public T with(long id) {
            this.id = id;
            return (T) this;
        }

        public T with(String name, String comm) {
            this.name = name;
            this.comm = comm;
            return (T) this;
        }

        public T with(double rating) {
            this.rating = rating;
            return (T) this;
        }

        public T with(float lat, float lng) {
            this.lat = lat;
            this.lng = lng;
            return (T) this;
        }

        public T with(String images) {
            this.images = images;
            return (T) this;
        }

        public LocationEntry build() {
            LocationEntry entry = null;
            if(this.id != -1) {
                entry = new LocationEntry(this.id, this.name, this.comm, this.rating, this.lat, this.lng, this.images);
            } else {
                entry = new LocationEntry(this.name, this.comm, this.rating, this.lat, this.lng, this.images);
            }

            this.reset();
            return entry;
        }

        final void reset() {
            this.id = -1;
            this.name = null;
            this.comm = null;
            this.rating = -1;
            this.lat = 0;
            this.lng = 0;
        }
    }
}