/*
 * Licensed to Elastic (the "Author") under one
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

package org.elasticsearch.demo.workshop.injector.bean;

import java.text.DecimalFormat;

/*
    We need to flatten our model when using AppSearch
 */
public class AppSearchPerson extends Person {

    private DecimalFormat df = new DecimalFormat("#.############");
    private String country;
    private String zipcode;
    private String city;
    private String countrycode;
    private String location;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = df.format(location.getLat()) + "," + df.format(location.getLon());
    }

    public AppSearchPerson(Person source) {
        setName(source.getName());
        setDateOfBirth(source.getDateOfBirth());
        setChildren(source.getChildren());
        setGender(source.getGender());
        setCity(source.getAddress().getCity());
        setCountry(source.getAddress().getCountry());
        setCountrycode(source.getAddress().getCountrycode());
        setZipcode(source.getAddress().getZipcode());
        setLocation(source.getAddress().getLocation());
    }

    public Marketing getMarketing() {
        return null;
    }

    public Address getAddress() {
        return null;
    }
}
