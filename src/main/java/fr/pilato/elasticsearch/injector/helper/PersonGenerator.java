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

package fr.pilato.elasticsearch.injector.helper;


import fr.pilato.elasticsearch.injector.bean.Address;
import fr.pilato.elasticsearch.injector.bean.GeoPoint;
import fr.pilato.elasticsearch.injector.bean.Marketing;
import fr.pilato.elasticsearch.injector.bean.Person;
import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class PersonGenerator {

    private static ArrayList<String> names;

    static {
        try {
            PersonGenerator.names = CsvReader.readAsStrings("/prenoms.csv");
        } catch (IOException e) {
            System.err.println("Can not generate names from CSV");
        }
    }

    public static Person personGenerator() {
        Person person = new Person();
        buildGender(person);
        person.setDateOfBirth(buildBirthDate());
        person.setMarketing(buildMeta());
        person.setAddress(buildAddress());
        person.setChildren(buildChildren());

        return person;
    }

    private static Marketing buildMeta() {
        Marketing marketing = new Marketing();
        int nbMeta = numberGenerator(1, 5);

        for (int i = 0; i < nbMeta; i++) {
            int nbConsult = numberGenerator(30, 2000);
            int typeMeta = numberGenerator(0, 9);
            switch (typeMeta) {
                case 0:
                    marketing.setShoes(nbConsult);
                    break;
                case 1:
                    marketing.setToys(nbConsult);
                    break;
                case 2:
                    marketing.setFashion(nbConsult);
                    break;
                case 3:
                    marketing.setMusic(nbConsult);
                    break;
                case 4:
                    marketing.setGarden(nbConsult);
                    break;
                case 5:
                    marketing.setElectronic(nbConsult);
                    break;
                case 6:
                    marketing.setHifi(nbConsult);
                    break;
                case 7:
                    marketing.setCars(nbConsult);
                    break;
                case 8:
                    marketing.setFood(nbConsult);
                    break;
                default:
                    System.err.println("   ->" + typeMeta);
                    break;
            }


        }

        return marketing;

    }

    private static Date buildBirthDate() {
        String birthDate = "" + numberGenerator(1940, 70) + "-" + numberGenerator(1, 12) + "-" + numberGenerator(1, 28);
        Date date = null;
        try {
            date = DateUtils.parseDate(birthDate, "yyyy-MM-dd");
        } catch (ParseException e) {
            System.err.println("buildBirthDate ->" + birthDate);
        }
        return date;
    }

    private static void buildGender(Person person) {
        int pos = numberGenerator(0, names.size());

        String line = names.get(pos);
        ArrayList<String> temp =  CsvReader.extractFromCommas(line);
        person.setName(temp.get(0) + " " + CsvReader.extractFromCommas(
                names.get(numberGenerator(0, names.size()))).get(0));
        person.setGender(temp.get(1));
    }

    private static Address buildAddress() {
        Address address = new Address();
        generateCountry(address);
        long result = Math.round(Math.random() * 2);

        if ("FR".equals(address.getCountrycode())) {
            switch ((int) result) {
                case 0:
                    address.setCity("Paris");
                    address.setZipcode("75000");
                    address.setLocation(new GeoPoint(doubleGenerator(48.819918, 48.900552), doubleGenerator(2.25929, 2.4158559)));
                    break;
                case 1:
                    address.setCity("Nantes");
                    address.setZipcode("44000");
                    address.setLocation(new GeoPoint(doubleGenerator(47.157742, 47.270729), doubleGenerator(-1.623467, -1.471032)));
                    break;
                case 2:
                    address.setCity("Cergy");
                    address.setZipcode("95000");
                    address.setLocation(new GeoPoint(doubleGenerator(49.019583, 49.059419), doubleGenerator(2.003001, 2.090892)));
                    break;
                default:
                    System.err.println("buildAddress ->" + result);
                    break;
            }
        }

        if ("GB".equals(address.getCountrycode())) {
            switch ((int) result) {
                case 0:
                    address.setCity("London");
                    address.setZipcode("98888");
                    address.setLocation(new GeoPoint(doubleGenerator(51.444014, 51.607633), doubleGenerator(-0.294245, 0.064184)));
                    break;
                case 1:
                    address.setCity("Plymouth");
                    address.setZipcode("5226");
                    address.setLocation(new GeoPoint(doubleGenerator(50.345272, 50.434797), doubleGenerator(-4.190161, -4.034636)));
                    break;
                case 2:
                    address.setCity("Liverpool");
                    address.setZipcode("86767");
                    address.setLocation(new GeoPoint(doubleGenerator(53.345346, 53.496339), doubleGenerator(-3.047485, -2.564774)));
                    break;
                default:
                    System.err.println("buildAddress ->" + result);
                    break;
            }
        }

        if ("DE".equals(address.getCountrycode())) {
            switch ((int) result) {
                case 0:
                    address.setCity("Berlin");
                    address.setZipcode("9998");
                    address.setLocation(new GeoPoint(doubleGenerator(52.364796, 52.639827), doubleGenerator(13.115778, 13.769465)));
                    break;
                case 1:
                    address.setCity("Bonn");
                    address.setZipcode("0099");
                    address.setLocation(new GeoPoint(doubleGenerator(50.649948, 50.766049), doubleGenerator(7.025075, 7.214589)));
                    break;
                case 2:
                    address.setCity("Munich");
                    address.setZipcode("45445");
                    address.setLocation(new GeoPoint(doubleGenerator(48.081337, 48.238441), doubleGenerator(11.371548, 11.711437)));
                    break;
                default:
                    System.err.println("buildAddress ->" + result);
                    break;
            }
        }

        if ("IT".equals(address.getCountrycode())) {
            switch ((int) result) {
                case 0:
                    address.setCity("Rome");
                    address.setZipcode("00100");
                    address.setLocation(new GeoPoint(doubleGenerator(41.797211, 41.980805), doubleGenerator(12.373950, 12.601393)));
                    break;
                case 1:
                    address.setCity("Turin");
                    address.setZipcode("10100");
                    address.setLocation(new GeoPoint(doubleGenerator(45.007912, 45.122125), doubleGenerator(7.593528, 7.747337)));
                    break;
                case 2:
                    address.setCity("Ischia");
                    address.setZipcode("80100");
                    address.setLocation(new GeoPoint(doubleGenerator(40.704982, 40.758477), doubleGenerator(13.859360, 13.953002)));
                    break;
                default:
                    System.err.println("buildAddress ->" + result);
                    break;
            }
        }

        return address;
    }

    private static void generateCountry(Address address) {

        int result = numberGenerator(0,4);

        switch (result) {
            case 0:
                address.setCountry("France");
                address.setCountrycode("FR");
                break;
            case 1:
                address.setCountry("Germany");
                address.setCountrycode("DE");
                break;
            case 2:
                address.setCountry("England");
                address.setCountrycode("GB");
                break;
            case 3:
                address.setCountry("Italy");
                address.setCountrycode("IT");
                break;
            default:
                System.err.println("generateCountry ->" + result);
                break;
        }

    }

    private static Integer buildChildren() {
        return numberGenerator(0,5);
    }

    private static int numberGenerator(int min, int range) {
        return (int) Math.floor(Math.random()*range+min);
    }

    private static double doubleGenerator(double min, double max) {
        return min + (max - min) * new Random().nextDouble();
    }
}
