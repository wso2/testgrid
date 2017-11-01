package org.wso2.carbon.testgrid.utils;

/**
 * This Utility class is used to access Environment variables.
 */
public class EnvVariableUtil {

    public static String readEnvironmentVariable(String variable) {
        return System.getenv(variable);
    }
}
