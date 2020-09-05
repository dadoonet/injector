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

package org.elasticsearch.demo.workshop.injector.bean;

/**
 * We define here marketing meta data:
 * Number of clicks on each segment
 */
public class Marketing {
    private Integer cars;
    private Integer shoes;
    private Integer toys;
    private Integer fashion;
    private Integer music;
    private Integer garden;
    private Integer electronic;
    private Integer hifi;
    private Integer food;

    public Integer getCars() {
        return cars;
    }

    public void setCars(Integer cars) {
        this.cars = cars;
    }

    public Integer getShoes() {
        return shoes;
    }

    public void setShoes(Integer shoes) {
        this.shoes = shoes;
    }

    public Integer getToys() {
        return toys;
    }

    public void setToys(Integer toys) {
        this.toys = toys;
    }

    public Integer getFashion() {
        return fashion;
    }

    public void setFashion(Integer fashion) {
        this.fashion = fashion;
    }

    public Integer getMusic() {
        return music;
    }

    public void setMusic(Integer music) {
        this.music = music;
    }

    public Integer getGarden() {
        return garden;
    }

    public void setGarden(Integer garden) {
        this.garden = garden;
    }

    public Integer getElectronic() {
        return electronic;
    }

    public void setElectronic(Integer electronic) {
        this.electronic = electronic;
    }

    public Integer getHifi() {
        return hifi;
    }

    public void setHifi(Integer hifi) {
        this.hifi = hifi;
    }

    public Integer getFood() {
        return food;
    }

    public void setFood(Integer food) {
        this.food = food;
    }
}
