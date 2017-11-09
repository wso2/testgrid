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
package org.wso2.carbon.testgrid.reporting.util;

import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import javax.lang.model.SourceVersion;

/**
 * Class to handle java reflection activities.
 *
 * @since 1.0.0
 */
public class ReflectionUtil {

    /**
     * Returns an instance from the given class type.
     *
     * @param type class to create an instance from
     * @return instance from the given class type
     * @throws ReportingException thrown when the default constructor is not found or when the default
     *                            constructor is not accessible or when an error occurs during instantiating the
     *                            object from class
     */
    public static <T> T createInstanceFromClass(Class<T> type) throws ReportingException {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                try {
                    return type.cast(constructor.newInstance());
                } catch (InstantiationException e) {
                    throw new ReportingException(String.format(Locale.ENGLISH,
                            "Error occurred in instantiating an instance from %s", type.getName()));
                } catch (IllegalAccessException e) {
                    throw new ReportingException(String.format(Locale.ENGLISH,
                            "Cannot access default constructor in %s", type.getName()));
                } catch (InvocationTargetException e) {
                    throw new ReportingException(String.format(Locale.ENGLISH,
                            "Error occurred when invoking default constructor in %s", type.getName()));
                }
            }
        }
        throw new ReportingException(String.format(Locale.ENGLISH,
                "Default constructor not found in %s", type.getName()));
    }

    /**
     * Sets a given value to a given field in a given class.
     *
     * @param classObject Object in which the value should be set to the field
     * @param fieldName   Field in which the given value should be set to
     * @param value       value to be set to the given field in the given class
     * @param <T>         Type of class in which the value should be set to the field
     * @throws ReportingException thrown when the field to set the value is not present in the class
     */
    public static <T> void setFieldValue(T classObject, String fieldName, Object value) throws ReportingException {
        Field field = getClassField(classObject, fieldName);

        try {
            field.set(classObject, field.getType().cast(value));
        } catch (IllegalAccessException e) {
            throw new ReportingException(String.format(Locale.ENGLISH,
                    "Error in setting field value %s to field %s", value, fieldName));
        }
    }

    /**
     * Returns the field with the given field name of the given class.
     *
     * @param classObject instance of the class to obtain the given field
     * @param fieldName   name of the field to be returned
     * @param <T>         type of the instance of the class to obtain the given field
     * @return the field with the given field name of the given class
     * @throws ReportingException thrown when the field is not found in the class
     */
    public static <T> Field getClassField(T classObject, String fieldName) throws ReportingException {
        if (!SourceVersion.isName(fieldName)) {
            throw new ReportingException(String.format(Locale.ENGLISH, "Field name %s is not valid", fieldName));
        }
        Optional<Field> field = Arrays.stream(classObject.getClass().getDeclaredFields())
                .filter(fieldEntry -> fieldEntry.getName().equalsIgnoreCase(fieldName))
                .findFirst();
        if (!field.isPresent()) {
            throw new ReportingException(String.format(Locale.ENGLISH,
                    "Field %s not found in %s", fieldName, classObject.getClass()));
        }
        field.get().setAccessible(true);
        return field.get();
    }
}