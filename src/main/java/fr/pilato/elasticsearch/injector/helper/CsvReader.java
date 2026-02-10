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


import java.io.*;
import java.util.ArrayList;

/**
 * Reads CSV resources from the classpath.
 */
public class CsvReader {

    /** Private constructor for utility class. */
    private CsvReader() {
    }

    /**
     * Returns all lines from a classpath resource as strings.
     * @param url classpath resource path (e.g. /prenoms.csv)
     * @return list of lines
     * @throws IOException if the resource cannot be read
     */
    public static ArrayList<String> readAsStrings(String url) throws IOException {
        /*returns all of the data in a file as Strings given the File object*/
        ArrayList<String> data = new ArrayList<>();
        InputStream ips= CsvReader.class.getResourceAsStream(url);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader reader = new BufferedReader(ipsr);
        String nextLine = reader.readLine();
        while (nextLine != null) {
            data.add(nextLine);
            nextLine = reader.readLine();
        }
        reader.close();//just a good idea apparently

        return data;
    }

    /**
     * Splits a comma-separated line into a list of values.
     * @param dataLine the line to split
     * @return list of values between commas
     */
    public static ArrayList<String> extractFromCommas(String dataLine) {
        //Gives back the data that is found between commas in a String
        ArrayList<String> data = new ArrayList<>();
        StringBuilder theString = new StringBuilder();
        for (int i = 0; i < dataLine.length(); i++) { //go down the whole string
            if (dataLine.charAt(i) == ',') {
                if (i != 0) {
                    data.add(theString.toString()); //this means that the next comma has been reached
                    theString = new StringBuilder(); //reset theString Variable
                }
            } else {
                theString.append(dataLine.charAt(i)); //otherwise, just keep piling the chars onto the cumulative string
            }
        }
        if (!theString.toString().equalsIgnoreCase("")) //only if the last position is not occupied with nothing then add the end on
        {
            data.add(theString.toString());
        }
        return data;
    }
}
