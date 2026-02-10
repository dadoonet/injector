/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.injector.bean;

/**
 * Represents a geographic point with latitude and longitude.
 */
public class GeoPoint {
    private double lon;
    private double lat;

    /** Default constructor for geo point. */
    public GeoPoint() {
    }

    /**
     * Creates a geo point with the given coordinates.
     * @param lat latitude
     * @param lon longitude
     */
    public GeoPoint(double lat, double lon) {
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * Returns the longitude.
     * @return the longitude
     */
    public double getLon() {
        return lon;
    }

    /**
     * Sets the longitude.
     * @param lon the longitude
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Returns the latitude.
     * @return the latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * Sets the latitude.
     * @param lat the latitude
     */
    public void setLat(double lat) {
        this.lat = lat;
    }
}
