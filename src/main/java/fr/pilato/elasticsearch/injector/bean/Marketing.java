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

    /** @return number of clicks on cars segment */
    public Integer getCars() {
        return cars;
    }

    /** @param cars number of clicks on cars segment */
    public void setCars(Integer cars) {
        this.cars = cars;
    }

    /** @return number of clicks on shoes segment */
    public Integer getShoes() {
        return shoes;
    }

    /** @param shoes number of clicks on shoes segment */
    public void setShoes(Integer shoes) {
        this.shoes = shoes;
    }

    /** @return number of clicks on toys segment */
    public Integer getToys() {
        return toys;
    }

    /** @param toys number of clicks on toys segment */
    public void setToys(Integer toys) {
        this.toys = toys;
    }

    /** @return number of clicks on fashion segment */
    public Integer getFashion() {
        return fashion;
    }

    /** @param fashion number of clicks on fashion segment */
    public void setFashion(Integer fashion) {
        this.fashion = fashion;
    }

    /** @return number of clicks on music segment */
    public Integer getMusic() {
        return music;
    }

    /** @param music number of clicks on music segment */
    public void setMusic(Integer music) {
        this.music = music;
    }

    /** @return number of clicks on garden segment */
    public Integer getGarden() {
        return garden;
    }

    /** @param garden number of clicks on garden segment */
    public void setGarden(Integer garden) {
        this.garden = garden;
    }

    /** @return number of clicks on electronic segment */
    public Integer getElectronic() {
        return electronic;
    }

    /** @param electronic number of clicks on electronic segment */
    public void setElectronic(Integer electronic) {
        this.electronic = electronic;
    }

    /** @return number of clicks on hifi segment */
    public Integer getHifi() {
        return hifi;
    }

    /** @param hifi number of clicks on hifi segment */
    public void setHifi(Integer hifi) {
        this.hifi = hifi;
    }

    /** @return number of clicks on food segment */
    public Integer getFood() {
        return food;
    }

    /** @param food number of clicks on food segment */
    public void setFood(Integer food) {
        this.food = food;
    }
}
