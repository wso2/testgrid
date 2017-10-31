package org.wso2.carbon.testgrid.utils;

/**
 * Created by sameera on 30/10/17.
 */
public class EnvVariableUtil {
    public static String readEnvironmentVariable(String variable){
        return System.getenv(variable);
    }
}
