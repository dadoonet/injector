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
 * We define here marketing meta data:
 * Number of clicks on each segment
 */
public class Marketing {
    private Integer cars;

    /** Default constructor for marketing data. */
    public Marketing() {
    }

    private Integer shoes;
    private Integer toys;
    private Integer fashion;
    private Integer music;
    private Integer garden;
    private Integer electronic;
    private Integer hifi;
    private Integer food;

    /**
     * Returns the number of clicks on the cars segment.
     * @return number of clicks on cars segment
     */
    public Integer getCars() {
        return cars;
    }

    /**
     * Sets the number of clicks on the cars segment.
     * @param cars number of clicks on cars segment
     */
    public void setCars(Integer cars) {
        this.cars = cars;
    }

    /**
     * Returns the number of clicks on the shoes segment.
     * @return number of clicks on shoes segment
     */
    public Integer getShoes() {
        return shoes;
    }

    /**
     * Sets the number of clicks on the shoes segment.
     * @param shoes number of clicks on shoes segment
     */
    public void setShoes(Integer shoes) {
        this.shoes = shoes;
    }

    /**
     * Returns the number of clicks on the toys segment.
     * @return number of clicks on toys segment
     */
    public Integer getToys() {
        return toys;
    }

    /**
     * Sets the number of clicks on the toys segment.
     * @param toys number of clicks on toys segment
     */
    public void setToys(Integer toys) {
        this.toys = toys;
    }

    /**
     * Returns the number of clicks on the fashion segment.
     * @return number of clicks on fashion segment
     */
    public Integer getFashion() {
        return fashion;
    }

    /**
     * Sets the number of clicks on the fashion segment.
     * @param fashion number of clicks on fashion segment
     */
    public void setFashion(Integer fashion) {
        this.fashion = fashion;
    }

    /**
     * Returns the number of clicks on the music segment.
     * @return number of clicks on music segment
     */
    public Integer getMusic() {
        return music;
    }

    /**
     * Sets the number of clicks on the music segment.
     * @param music number of clicks on music segment
     */
    public void setMusic(Integer music) {
        this.music = music;
    }

    /**
     * Returns the number of clicks on the garden segment.
     * @return number of clicks on garden segment
     */
    public Integer getGarden() {
        return garden;
    }

    /**
     * Sets the number of clicks on the garden segment.
     * @param garden number of clicks on garden segment
     */
    public void setGarden(Integer garden) {
        this.garden = garden;
    }

    /**
     * Returns the number of clicks on the electronic segment.
     * @return number of clicks on electronic segment
     */
    public Integer getElectronic() {
        return electronic;
    }

    /**
     * Sets the number of clicks on the electronic segment.
     * @param electronic number of clicks on electronic segment
     */
    public void setElectronic(Integer electronic) {
        this.electronic = electronic;
    }

    /**
     * Returns the number of clicks on the hifi segment.
     * @return number of clicks on hifi segment
     */
    public Integer getHifi() {
        return hifi;
    }

    /**
     * Sets the number of clicks on the hifi segment.
     * @param hifi number of clicks on hifi segment
     */
    public void setHifi(Integer hifi) {
        this.hifi = hifi;
    }

    /**
     * Returns the number of clicks on the food segment.
     * @return number of clicks on food segment
     */
    public Integer getFood() {
        return food;
    }

    /**
     * Sets the number of clicks on the food segment.
     * @param food number of clicks on food segment
     */
    public void setFood(Integer food) {
        this.food = food;
    }
}
