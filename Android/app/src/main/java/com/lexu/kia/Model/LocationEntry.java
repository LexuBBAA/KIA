package com.lexu.kia.Model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LocationEntry {

    private static final String TAG = "LocationEntry";

    private long m_ID = -1;
    private String mName = null;
    private String mComments = null;
    private double mRating = -1;
    private float mLatitude = 0;
    private float mLongitude = 0;

    private  LocationEntry(@NonNull String name, @Nullable String comments, double rating, float lat, float lng) {
        mName = name;
        mComments = comments;
        mRating = rating;
        mLatitude = lat;
        mLongitude = lng;
    }

    private LocationEntry(long id, @NonNull String name, @Nullable String comments, double rating, float lat, float lng) {
        m_ID = id;
        mName = name;
        mComments = comments;
        mRating = rating;
        mLatitude = lat;
        mLongitude = lng;
    }

    public long getM_ID() {
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

    protected static abstract class Builder {
        private static final String TAG = "Builder";

        long id = -1;
        String name = null;
        String comm = null;
        double rating = -1;
        float lat = 0;
        float lng = 0;

        Builder() {
        }

        public final Builder with(long id) {
            this.id = id;
            return this;
        }

        public final Builder with(String name, String comm) {
            this.name = name;
            this.comm = comm;
            return this;
        }

        public final Builder with(double rating) {
            this.rating = rating;
            return this;
        }

        public final Builder with(float lat, float lng) {
            this.lat = lat;
            this.lng = lng;
            return this;
        }

        public LocationEntry build() {
            LocationEntry entry = null;
            if(this.id != -1) {
                entry = new LocationEntry(this.id, this.name, this.comm, this.rating, this.lat, this.lng);
            } else {
                entry = new LocationEntry(this.name, this.comm, this.rating, this.lat, this.lng);
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

final class Builder extends LocationEntry.Builder {

    private LocationEntry entry = null;

    private Builder() {
        super();
    }

    public LocationEntry.Builder update(LocationEntry entry) {
        this.entry = entry;
        return this;
    }

    @Override
    public LocationEntry build() {
        if(this.id != -1) {
            this.entry.setId(this.id);
        }

        if(this.name != null) {
            this.entry.setName(this.name);
        }

        if(this.comm != null) {
            this.entry.setComments(this.comm);
        }

        if(this.rating != -1) {
            this.entry.setRating(this.rating);
        }

        if(this.lat != Float.MIN_VALUE) {
            this.entry.setLatitude(this.lat);
        }

        if(this.lng != Float.MIN_VALUE) {
            this.entry.setLongitude(this.lng);
        }

        this.reset();
        return this.entry;
    }
}