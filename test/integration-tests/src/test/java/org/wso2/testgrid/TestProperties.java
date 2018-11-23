package org.wso2.testgrid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {

    public static String email;
    public static String emailPassword;
    public static String jenkinsUser;
    public static String jenkinsToken;
    public static String jenkinsUrl = "https://testgrid-live-dev.private.wso2.com/admin";
//    public static String trigger = "https://testgrid-live-dev.private.wso2.com/admin/job/Phase-1/build";
//    public static String buildStatusUrl = "/job/Phase-1/lastBuild/api/json";

    private String propFileName = System.getenv("TEST_PROPS");

    public TestProperties() {

        getPropValues();
    }

    private void getPropValues() {

        try (InputStream inputStream = new FileInputStream(new File(propFileName))) {
            Properties prop = new Properties();

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found.");
            }

            email = prop.getProperty("email");
            emailPassword = prop.getProperty("emailPassword");
            jenkinsToken = prop.getProperty("jenkinsToken");
            jenkinsUser = prop.getProperty("jenkinsUser");

        } catch (IOException e) {
//            Log error
        }
    }
}
