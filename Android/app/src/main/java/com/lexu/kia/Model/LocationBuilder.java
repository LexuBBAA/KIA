/*
 * Copyright (c) Bogdan Andrei Alexandru Birsasteanu 2018.
 * All rights reserved.
 */

package com.lexu.kia.Model;

public class LocationBuilder extends LocationEntry.AbstractBuilder<LocationBuilder> {

    private LocationEntry entry = null;

    public LocationBuilder() {
        super();
    }

    @Override
    public LocationBuilder with(long id) {
        super.with(id);
        return this;
    }

    @Override
    public LocationBuilder with(String name, String comm) {
        super.with(name, comm);
        return this;
    }

    @Override
    public LocationBuilder with(double rating) {
        super.with(rating);
        return this;
    }

    @Override
    public LocationBuilder with(float lat, float lng) {
        super.with(lat, lng);
        return this;
    }

    @Override
    public LocationBuilder with(String images) {
        super.with(images);
        return this;
    }

    public LocationBuilder update(LocationEntry entry) {
        this.entry = entry;
        return this;
    }

    @Override
    public LocationEntry build() {
        if (entry == null) {
            return super.build();
        }

        if (this.id != -1) {
            this.entry.setId(this.id);
        }

        if (this.name != null) {
            this.entry.setName(this.name);
        }

        if (this.comm != null) {
            this.entry.setComments(this.comm);
        }

        if (this.rating != -1) {
            this.entry.setRating(this.rating);
        }

        if (this.lat != Float.MIN_VALUE) {
            this.entry.setLatitude(this.lat);
        }

        if (this.lng != Float.MIN_VALUE) {
            this.entry.setLongitude(this.lng);
        }

        this.reset();
        return this.entry;
    }
}
