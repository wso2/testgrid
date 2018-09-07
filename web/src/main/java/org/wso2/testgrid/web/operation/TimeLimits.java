package org.wso2.testgrid.web.operation;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * this class is to hold the data returned from influxDB
 */
@Measurement(name = "mem")
public class TimeLimits {
    @Column(name = "time")
    private Instant time;
    @Column(name = "value")
    private Double value;

    public Instant getTime() {
        return time;
    }
}
