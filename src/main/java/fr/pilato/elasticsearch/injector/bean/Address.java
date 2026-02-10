/*
 * Licensed to Tugdual Grall and David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
 * Represents a physical address with country, zip code, city and optional geo location.
 */
public class Address {

    private String country;
    private String zipcode;
    private String city;
    private String countrycode;
    private GeoPoint location;

    /** Default constructor for address. */
    public Address() {
    }

    /**
     * Returns the country.
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country.
     * @param country the country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the zip code.
     * @return the zip code
     */
    public String getZipcode() {
        return zipcode;
    }

    /**
     * Sets the zip code.
     * @param zipcode the zip code
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    /**
     * Returns the city.
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city the city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the country code (e.g. FR, DE).
     * @return the country code
     */
    public String getCountrycode() {
        return countrycode;
    }

    /**
     * Sets the country code.
     * @param countrycode the country code
     */
    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    /**
     * Returns the geo location.
     * @return the location or null
     */
    public GeoPoint getLocation() {
        return location;
    }

    /**
     * Sets the geo location.
     * @param location the location
     */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
