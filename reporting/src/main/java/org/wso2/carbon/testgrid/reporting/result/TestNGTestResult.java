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
package org.wso2.carbon.testgrid.reporting.result;

import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Result model to capture testng test result.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "test-method")
public class TestNGTestResult implements TestResultable {

    private static final String OUTPUT_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String TESTNG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @XmlAttribute(name = "status")
    private String status;

    @XmlAttribute(name = "signature")
    private String signature;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "is-config")
    private String isConfig = "false";

    @XmlAttribute(name = "duration-ms")
    private String durationMs;

    @XmlAttribute(name = "started-at")
    private String startedAt;

    @XmlAttribute(name = "finished-at")
    private String finishedAt;

    @Override
    public boolean isTestSuccess() {
        return this.status.equalsIgnoreCase("PASS");
    }

    @Override
    public String getTimestamp() {
        return null;
    }

    @Override
    public String getFormattedTimestamp() throws ReportingException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OUTPUT_DATE_FORMAT);
        SimpleDateFormat testNGDateFormat = new SimpleDateFormat(TESTNG_DATE_FORMAT);

        String[] dateElements = startedAt.split("T");
        String year = dateElements[0];
        String time = dateElements[1].split("Z")[0];

        Date date;
        try {
            date = testNGDateFormat.parse(year + " " + time);
        } catch (ParseException e) {
            throw new ReportingException(String.format(Locale.ENGLISH, "Error in parsing date %s", startedAt));
        }
        return simpleDateFormat.format(date);
    }

    @Override
    public String getTestCase() {
        return this.name;
    }

    @Override
    public String getFailureMessage() {
        return null;
    }
}
