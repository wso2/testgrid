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
package org.wso2.testgrid.logging;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class to log persistence log messages through SLF4J.
 *
 * The following property is used in persistence.xml file for using SLF4J
 * {@code <property name="eclipselink.logging.logger" value="org.wso2.testgrid.logging.Slf4jSessionLogger" />}
 */
public class Slf4jSessionLogger extends AbstractSessionLog {
   public static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging";
   public static final String DEFAULT_CATEGORY = "default";
   public static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY;
   private static Map<Integer, LogLevel> mapLevels = new HashMap<Integer, LogLevel>();

    /**
     * Stores all the java.util.logging.Levels with matching Slf4j level.
     */
   static {
      mapLevels.put(SessionLog.ALL, LogLevel.TRACE);
      mapLevels.put(SessionLog.FINEST, LogLevel.TRACE);
      mapLevels.put(SessionLog.FINER, LogLevel.TRACE);
      mapLevels.put(SessionLog.FINE, LogLevel.DEBUG);
      mapLevels.put(SessionLog.CONFIG, LogLevel.INFO);
      mapLevels.put(SessionLog.INFO, LogLevel.INFO);
      mapLevels.put(SessionLog.WARNING, LogLevel.WARN);
      mapLevels.put(SessionLog.SEVERE, LogLevel.ERROR);
   }
   
   private Map<String, Logger> categoryLoggers = new HashMap<String, Logger>();

   /**
    * Creates a new instance of Slf4jSessionLogger
    */
   public Slf4jSessionLogger() {
      super();
       for (String category : SessionLog.loggerCatagories) {
           addLogger(category, ECLIPSELINK_NAMESPACE + "." + category);
       }
       addLogger(DEFAULT_CATEGORY, DEFAULT_ECLIPSELINK_NAMESPACE);
   }

   @Override
   public void log(SessionLogEntry entry) {
      if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
         return;
      }
      Logger logger = getLogger(entry.getNameSpace());
      LogLevel logLevel = getLogLevel(entry.getLevel());
      StringBuilder message = new StringBuilder();
      message.append(getSupplementDetailString(entry));
      message.append(formatMessage(entry));
      switch (logLevel) {
      case TRACE:
         logger.trace(message.toString());
         break;
      case DEBUG:
         logger.debug(message.toString());
         break;
      case WARN:
         logger.warn(message.toString());
         break;
      case ERROR:
         logger.error(message.toString());
         break;
      default :
         logger.info(message.toString());
         break;
      }
   }

   @Override
   public boolean shouldLog(int level, String category) {
      Logger logger = getLogger(category);
      LogLevel logLevel = getLogLevel(level);
      switch (logLevel) {
      case TRACE:
         return logger.isTraceEnabled();
      case DEBUG:
         return logger.isDebugEnabled();
      case INFO:
         return logger.isInfoEnabled();
      case WARN:
         return logger.isWarnEnabled();
      case ERROR:
         return logger.isErrorEnabled();
      default:
         return true;
      }
   }

   @Override
   public boolean shouldLog(int level) {
      return shouldLog(level, "default");
   }

   @Override
   public boolean shouldDisplayData() {
      if (this.shouldDisplayData != null) {
         return shouldDisplayData.booleanValue();
      } else {
         return false;
      }
   }

    /**
     * Adds a logger to the categoryLoggers.
     *
     * @param loggerCategory category of the logger
     * @param loggerNameSpace namespace of the logger
     */
   private void addLogger(String loggerCategory, String loggerNameSpace) {
      categoryLoggers.put(loggerCategory,
            LoggerFactory.getLogger(loggerNameSpace));
   }

    /**
     * Gets a logger from categoryLoggers.
     *
     * @param category string representation of an EclipseLink category, e.g. "sql", "transaction"
     * @return         logger of the relevant category
     */
   private Logger getLogger(String category) {
      if (category == null
            || !this.categoryLoggers.containsKey(category)) {
         category = DEFAULT_CATEGORY;
      }
      return categoryLoggers.get(category);
   }

    /**
     * @param level the index of EclipseLink logging levels
     * @return      slf4J log level
     */
   private LogLevel getLogLevel(Integer level) {
      LogLevel logLevel = mapLevels.get(level);

      if (logLevel == null) {
         logLevel = LogLevel.OFF;
      }
      return logLevel;
   }

   /**
    * SLF4J log levels.
    */
   enum LogLevel {
      TRACE, DEBUG, INFO, WARN, ERROR, OFF
   }
}
