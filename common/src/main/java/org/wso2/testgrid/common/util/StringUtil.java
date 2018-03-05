/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Random;

/**
 * Utility class to handle {@link String} related operations.
 *
 * @since 1.0.0
 */
public class StringUtil {

    /**
     * Returns whether the given string is a null or empty.
     *
     * @param string string to check whether null or empty
     * @return returns {@code true} if the string is null or empty, {@code false} otherwise
     */
    public static boolean isStringNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Returns a string concatenating the given strings.
     *
     * @param objects list of objects to concatenate as strings
     * @return concatenated string
     */
    public static String concatStrings(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(object); // Null is handled by the append method.
        }
        return stringBuilder.toString();
    }

    /**
     * Generates a random string with the given character count using the below charset as input:
     * <i>ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890</i>
     * @param characterCount number of characters
     * @return a random string
     */
    public static String generateRandomString(int characterCount) {
        final String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < characterCount) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    /**
     * Generate a string which include all the properties (key and the value).
     * @param properties Properties
     * @return List of properties (key and value) as a String
     */
    public static String getPropertiesAsString(Properties properties) {
        if (properties.isEmpty()) {
            return "Empty property-list.";
        } else {
            StringWriter writer = new StringWriter();
            properties.list(new PrintWriter(writer));
            return writer.getBuffer().toString();
        }
    }

}
