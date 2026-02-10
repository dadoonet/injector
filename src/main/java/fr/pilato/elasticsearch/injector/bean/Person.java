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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.pilato.elasticsearch.injector.serializer.CustomDateSerializer;

import java.util.Date;

/**
 * Represents a person with name, birth date, gender, address and optional marketing data.
 */
public class Person {

    private String name = null;
    private Date dateOfBirth = null;
    private String gender = null;
    private Integer children;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Marketing marketing;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Address address;

    /** Default constructor for person. */
    public Person() {
    }

    /**
     * Gets name.
     * @return the person's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * @param name the person's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the date of birth.
     * @return the date of birth
     */
    @JsonSerialize(using = CustomDateSerializer.class)
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the date of birth.
     * @param dateOfBirth the date of birth
     */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns the gender.
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the gender.
     * @param gender the gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Returns the marketing data.
     * @return the marketing data or null
     */
    public Marketing getMarketing() {
        return marketing;
    }

    /**
     * Sets the marketing data.
     * @param marketing the marketing data
     */
    public void setMarketing(Marketing marketing) {
        this.marketing = marketing;
    }

    /**
     * Returns the address.
     * @return the address or null
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * @param address the address
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Returns the number of children.
     * @return the number of children
     */
    public Integer getChildren() {
        return children;
    }

    /**
     * Sets the number of children.
     * @param children the number of children
     */
    public void setChildren(Integer children) {
        this.children = children;
    }
}
