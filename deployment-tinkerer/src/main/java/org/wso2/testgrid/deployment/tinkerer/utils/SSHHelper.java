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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.deployment.tinkerer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.deployment.tinkerer.exception.DeploymentTinkererException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Base64;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * This class contains the SSH config file operations.
 * The file is located at the path "~/.ssh/config"
 *
 * @since 1.0.0
 */
public class SSHHelper {

    private static final Logger logger = LoggerFactory.getLogger(SSHHelper.class);

    /**
     * Adds and entry to the ssh config file  that will enable direct access to the instance
     * via the bastian instance.
     * <p>
     * The instance will contain a proxy access entry that will route the connection via the bastian
     * instance.
     *
     * @param keyContent the content of the security key file (.pem file) that is used to access the instance
     * @param bastianIP  The IP address of the bastian instance.
     * @param agent      The {@link Agent} object corresponding to the remote agent running on the instance
     * @throws IOException When there is an error creating the config entry
     */
    public static void addConfigEntry(String keyContent, String bastianIP, Agent agent) throws
            IOException, DeploymentTinkererException {

        Path config = Paths.get(System.getProperty("user.home"), ".ssh", "config");
        Path keyPath = Paths.get(System.getProperty("user.home"), ".ssh", agent.getInstanceId() + "-key.pem");
        saveKeyFile(keyContent, keyPath);

        File file = config.toFile();
        //Create the file if it does not exist
        if (!Files.exists(config) || !Files.isRegularFile(config)) {
            boolean mkdirs = file.getParentFile().mkdirs();
            boolean newFile = file.createNewFile();
            if (mkdirs || newFile) {
                logger.info("Created new ssh config file");
            }
        }
        String bastianEntry = "bastian:" + agent.getTestPlanId();
        String hostEntry = "host:" + agent.getInstanceName();
        if (bastianIP != null) {
            //check if the bastian entry has been made
            if (!containsEntry(file, bastianEntry)) {
                //add entry
                String entry = String.format("Host %s%n" +
                                "  StrictHostKeyChecking=no%n" +
                                "  UserKnownHostsFile=/dev/null%n" +
                                "  User ubuntu%n" +
                                "  HostName %s%n" +
                                "  IdentityFile %s%n",
                        bastianEntry, bastianIP, keyPath.toAbsolutePath().toString());
                Files.write(config, entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }
            if (!containsEntry(file, hostEntry)) {
                if (agent.getInstanceUser() != null) {
                    //add Host entry
                    String entry = String.format("%n%nHost %s%n  StrictHostKeyChecking=no%n" +
                                    "  UserKnownHostsFile=/dev/null%n" +
                                    "  User %s%n" +
                                    "  HostName %s%n" +
                                    "  IdentityFile %s%n" +
                                    "ProxyCommand ssh %s@%s -W %%h:%%p%n"
                            , hostEntry, agent.getInstanceUser(), agent.getInstanceIp(),
                            keyPath.toAbsolutePath().toString(), agent.getInstanceUser(), bastianEntry);
                    Files.write(config, entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } else {
                    throw new DeploymentTinkererException("Host entry value is null, Please make sure that" +
                            "instance user is correctly set according to the cloud provider!");
                }
            }
        } else {
            if (!containsEntry(file, agent.getInstanceId())) {
                if (agent.getInstanceUser() != null) {
                    //add Host entry
                    String entry = String.format("Host %s%n  StrictHostKeyChecking=no%n" +
                                    "  UserKnownHostsFile=/dev/null%n" +
                                    "  User %s%n" +
                                    "  HostName %s%n" +
                                    "  IdentityFile %s%n" +
                                    "  "
                            , hostEntry, agent.getInstanceUser(), agent.getInstanceIp(),
                            keyPath.toAbsolutePath().toString());
                    Files.write(config, entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } else {
                    throw new DeploymentTinkererException("Host entry value is null, Please make sure that" +
                            "instance user is correctly set according to the cloud provider!");
                }
            }
        }
    }

    /**
     * Checks if a particular host entry has already been made in the config file
     *
     * @param file  File object for the ssh config file
     * @param entry Entry to be checked
     * @return true if the entry is already existing , false otherwise
     * @throws FileNotFoundException when there is an error finding the config file
     */
    private static boolean containsEntry(File file, String entry) throws FileNotFoundException {
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(entry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the security key file in the local filesystem so it could be used when accessing the instances
     *
     * @param keyFile Base64 encoded Contents of the key file
     * @param path    Path where the key file will be saved
     * @throws IOException When there is an error saving the file.
     */
    public static void saveKeyFile(String keyFile, Path path) throws IOException {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            File file = path.toFile();
            boolean mkdirs = file.getParentFile().mkdirs();
            boolean newFile = file.createNewFile();
            if (mkdirs || newFile) {
                logger.info("Created new key file in location : " + path.toString());
            }
            Files.write(path, Base64.getDecoder().decode(keyFile));
            //change the file permission to owner read only i.e chomod 400
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            Files.setPosixFilePermissions(path, perms);
        }
    }
}


