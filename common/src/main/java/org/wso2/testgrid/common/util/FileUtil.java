/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class for handling file operations.
 *
 * @since 1.0.0
 */
public class FileUtil {

    /**
     * Returns an instance of the specified type from the given configuration YAML.
     *
     * @param location location of the configuration YAML file
     * @return instance of the specified type from the given configuration YAML
     * @throws IOException thrown when no file is found in the given location or when error on closing file input stream
     */
    public static <T> T readConfigurationFile(String location, Class<T> type) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(location))) {
            return new Yaml().loadAs(fileInputStream, type);
        }
    }
}
