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
package org.wso2.testgrid.web.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.web.utils.FileWatcher;
import org.wso2.testgrid.web.utils.FileWatcherException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Abstract event socket mediator to handle web socket endpoints.
 *
 * @since 1.0.0
 */
@ServerEndpoint("/live-log/{test-plan-id}")
public class LogEventSocketMediator {

    private static final Logger log = LoggerFactory.getLogger(LogEventSocketMediator.class);

    /**
     * Method called when a message is received from the client.
     *
     * @param message    message received from client
     * @param session    client session
     * @param testPlanId test plan id
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("test-plan-id") String testPlanId) {
        log.info(StringUtil.concatStrings("Received message {", message, "} from client ", session.getId()));
    }

    /**
     * Method called when opening the web socket.
     *
     * @param session    client session
     * @param testPlanId test plan ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("test-plan-id") String testPlanId) {
        try {
            log.info(StringUtil.concatStrings("Opened web socket channel for test plan id ", testPlanId));
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            Optional<TestPlan> testPlan = testPlanUOW.getTestPlanById(testPlanId);
            if (testPlan.isPresent()) {
                runLogTailer(testPlan.get(), session);
            } else {
                String message = StringUtil.concatStrings("Error: Test Plan for ID", testPlanId,
                        "do not exist.\nThis error should not occur");
                log.error(message);
                session.getBasicRemote().sendText(message);
            }
        } catch (TestGridDAOException e) {
            String message = StringUtil
                    .concatStrings("Error occurred while fetching the TestPlan for the given ID : '",
                            testPlanId, "'");
            log.error(message, e);
        } catch (IOException e) {
            log.error("Error on sending web socket message.", e);
        } catch (TestGridException e) {
            log.error("Error on calculating the log file path.", e);
        } catch (FileWatcherException e) {
            log.error("Error on reading watched file contents.", e);
        }
    }

    /**
     * Method called when closing the web socket.
     *
     * @param session    client session
     * @param testPlanId test plan ID
     */
    @OnClose
    public void onClose(Session session, @PathParam("test-plan-id") String testPlanId) {
        log.info(StringUtil.concatStrings("Closed web socket channel for test plan id ", testPlanId));
    }

    /**
     * Runs the log tailing process.
     *
     * @param testPlan test plan required to get the log file path
     * @param session  client session
     * @throws TestGridException    thrown when error on calculating the log file path
     * @throws IOException          thrown when error on registering watch file service
     * @throws FileWatcherException thrown when error on reading watched file contents
     */
    private void runLogTailer(TestPlan testPlan, Session session)
            throws TestGridException, IOException, FileWatcherException {
        Path logFilePath = getLogFilePath(testPlan);
        FileWatcher fileWatcher = new LogFileWatcher(logFilePath, session);
        new Thread(fileWatcher).start();
    }

    /**
     * Returns the path of the log file for the given test plan.
     *
     * @param testPlan test plan to read log file from
     * @return path of the log file related to the test plan
     * @throws TestGridException thrown when error on calculating the log file path
     */
    private Path getLogFilePath(TestPlan testPlan) throws TestGridException {
        return TestGridUtil.getTestRunWorkspace(testPlan, false).resolve(TestGridConstants.TEST_LOG_FILE_NAME);
    }

    /**
     * This class is responsible for watching changes of the given log file.
     *
     * @since 1.0.0
     */
    private static class LogFileWatcher extends FileWatcher {

        private final Session session;
        private String currentFileContents = "";

        /**
         * Creates an instance of {@link LogFileWatcher} to watch the given file.
         *
         * @param watchFile file to be watched
         * @param session   client session
         * @throws FileWatcherException thrown when error on creating an instance of {@link LogFileWatcher}
         */
        LogFileWatcher(Path watchFile, Session session) throws FileWatcherException {
            super(watchFile);
            this.session = session;
        }

        @Override
        public void beforeFileWatch(String fileContents) throws FileWatcherException {
            sendTextDiffToClient(fileContents);
        }

        @Override
        public void onCreate(String fileContents) throws FileWatcherException {
            sendTextDiffToClient(fileContents);
        }

        @Override
        public void onModified(String fileContents) throws FileWatcherException {
            sendTextDiffToClient(fileContents);
        }

        /**
         * Sends the file contents diff to client.
         *
         * @param fileContents file contents
         * @throws FileWatcherException thrown when error on sending file contents to the client
         */
        private void sendTextDiffToClient(String fileContents) throws FileWatcherException {
            try {
                String difference = org.apache.commons.lang3.StringUtils.difference(currentFileContents, fileContents);
                currentFileContents = fileContents;
                session.getBasicRemote().sendText(difference);
            } catch (IOException e) {
                throw new FileWatcherException("Error on sending file contents to the client.", e);
            }
        }

        @Override
        public void onDelete() {
            // Do nothing
        }
    }
}
