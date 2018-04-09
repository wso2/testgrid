package org.wso2.testgrid.common.config;

import java.io.Serializable;
import java.util.List;

/**
 * Defines the excluding infrastructures.
 * This configuration is required in order for
 * TestGrid to figure out infrastructures that we need to exclude when creating test plans.
 * <p>
 * For example, MySQL-7.0 exclude from test plans generating for product x.
 *
 * @since 1.0.0
 */
public class Exclude implements Serializable {

    private static final long serialVersionUID = -1661815137752094462L;

    private String name;
    private List<Double> versions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getVersions() {
        return versions;
    }

    public void setVersions(List<Double> versions) {
        this.versions = versions;
    }
}
