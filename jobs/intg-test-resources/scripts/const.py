# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

NS = {'d': 'http://maven.apache.org/POM/4.0.0', 'carbon':'http://wso2.org/projects/carbon/carbon.xml'}
VALUE_TAG = "{http://maven.apache.org/POM/4.0.0}value"
M2_PATH = {"product-is": "is/wso2is", "product-apim": "am/wso2am",
           "product-ei": "ei/wso2ei"}
DIST_POM_PATH = {"product-is": "modules/distribution/pom.xml", "product-apim": "modules/distribution/product/pom.xml",
                 "product-ei": "distribution/pom.xml"}

ZIP_FILE_EXTENSION = ".zip"
CARBON_NAME = "carbon.zip"

DATASOURCE_PATHS = {"product-apim": ["repository/conf/datasources/master-datasources.xml",
                                     "repository/conf/datasources/metrics-datasources.xml"],
                    "product-is": [],
                    "product-ei": []}
LIB_PATH = {"product-apim": "repository/components/lib", "product-is": "", "product-ei": ""}

CONFIG_PATHS = {"product-apim": { "REGISTRY_CONF": 'repository/conf/registry.xml',
                                  "CARBON_CONF": 'repository/conf/carbon.xml',
                                  "APIM_CONF": 'repository/conf/api-manager.xml',
                                         "USER_CONF": 'repository/conf/user-mgt.xml',
                                  "STORE_CONF": 'repository/deployment/server/jaggeryapps/store/site/conf/site.json',
                                  "PUBLISHER_CONF": 'repository/deployment/server/jaggeryapps/publisher/site/conf/site.json',
                                  "JNDI_CONF": 'repository/conf/jndi.properties'},
                       "product-is": { "REGISTRY_CONF": 'repository/conf/registry.xml',
                                       "CARBON_CONF": 'repository/conf/carbon.xml',
                                       "USER_CONF": 'repository/conf/user-mgt.xml'},
                        "product-ei": { "REGISTRY_CONF": 'repository/conf/registry.xml',
                                        "CARBON_CONF": 'repository/conf/carbon.xml',
                                        "USER_CONF": 'repository/conf/user-mgt.xml'}}

PRODUCT_STORAGE_DIR_NAME = "storage"
TEST_PLAN_PROPERTY_FILE_NAME = "testplan.properties"
LOG_FILE_NAME = "integration.log"
ORACLE_DB_ENGINE = "ORACLE-SE2"
MSSQL_DB_ENGINE = "SQLSERVER-SE"
MYSQL_DB_ENGINE = "MYSQL"
DEFAULT_ORACLE_SID = "orcl"
DEFAULT_DB_USERNAME = "wso2carbon"
LOG_STORAGE = "logs"

DB_META_DATA = {
    "MYSQL": {"prefix": "jdbc:mysql://", "driverClassName": "com.mysql.jdbc.Driver", "jarName": "mysql.jar",
              "DB_SETUP": {
                  "product-apim": {"WSO2UM_DB": ['mysql5.7.sql'], "WSO2AM_DB": ['apimgt/mysql5.7.sql'],
                                   "WSO2AM_STATS_DB": [], "WSO2REG_DB" : ['mysql5.7.sql'],
                                   "WSO2_MB_STORE_DB": ['mb-store/mysql-mb.sql'],
                                   "WSO2_METRICS_DB": ['metrics/mysql.sql']}, "product-is": {},
                  "product-ei": {}}},
    "SQLSERVER-SE": {"prefix": "jdbc:sqlserver://",
                     "driverClassName": "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jarName": "sqlserver-ex.jar",
                     "DB_SETUP": {
                         "product-apim": {"WSO2UM_DB": ['mssql.sql'], "WSO2AM_DB": ['apimgt/mssql.sql'],
                                          "WSO2AM_STATS_DB": [], "WSO2REG_DB" : ['mssql.sql'],
                                          "WSO2_MB_STORE_DB": ['mb-store/mssql-mb.sql'],
                                          "WSO2_METRICS_DB": ['metrics/mssql.sql']}, "product-is": {},
                         "product-ei": {}}},
    "ORACLE-SE2": {"prefix": "jdbc:oracle:thin:@", "driverClassName": "oracle.jdbc.OracleDriver",
                   "jarName": "oracle-se.jar",
                   "DB_SETUP": {
                       "product-apim": {"WSO2UM_DB": ['oracle.sql'], "WSO2AM_DB": ['apimgt/oracle.sql'],
                                        "WSO2AM_STATS_DB": [], "WSO2REG_DB" : ['oracle.sql'],
                                        "WSO2_MB_STORE_DB": ['mb-store/oracle-mb.sql'],
                                        "WSO2_METRICS_DB": ['metrics/oracle.sql']}, "product-is": {},
                       "product-ei": {}}},
    "POSTGRESQL": {"prefix": "jdbc:postgresql://", "driverClassName": "org.postgresql.Driver",
                   "jarName": "postgres.jar",
                   "DB_SETUP": {"product-apim": {"WSO2UM_DB": [], "WSO2AM_DB": [], "WSO2AM_STATS_DB": [],
                                                 "WSO2_MB_STORE_DB": [], "WSO2_METRICS_DB": [], "product-is": {},
                                                 "product-ei": {}}}
                   }}
OS_USER_DATA = {
        "CENTOS": "root",
        "WINDOWS": "Administrator",
        "UBUNTU": "ubuntu"
    }

RSYNC_CRON_SCRIPT_TEMPLATE = {
        "CENTOS": "rsync-for-carbon-depsync-unix.sh",
        "WINDOWS": "rsync-for-carbon-depsync-template-win"
    }

RSYNC_CRON_SCRIPT_FILE = {
        "CENTOS": "rsync-for-carbon-depsync-unix.sh",
        "WINDOWS": "rsync-for-carbon-depsync.ps"
    }

SYNC_DIRS = {"product-apim": ["/repository/deployment/server/", "/repository/tenants/"],
             "product-is": [],
             "product-ei": []
    }

SQL_DRIVER_PATH = {
        "CENTOS": "/home/centos/sql-drivers/",
        "WINDOWS": "/testgrid/sql-drivers"
    }

START_SCRIPT_PATH = {
    "product-apim": {
        "CENTOS": "bin/wso2server.sh",
        "WINDOWS": "bin/wso2server.bat"
    },
    "product-is": {},
    "product-ei": {}
    }

WORKER_LIST_FILENAME = "workers-list.txt"
NODE_IP_PREFIX = "10.0.1.1"
TESTGRID_DIR = "testgrid"

TYPE_RECEIVER = 'receiver'
TYPE_AUTH = 'auth'

STATIC_CONFIGS_DIR = "static-configs"
CRON_FILE = "cron-job.sh"

NODE_TYPE_MASTER = "MASTER"
NODE_TYPE_SLAVE = "SLAVE"

PORT_NUMBERS = {
        "MGT_HTTP": 9763,
        "MGT_HTTPS": 9443,
        "NIO_HTTP": 8280,
        "NIO_HTTPS": 8243,
        "THROTTLE_PUBLISHER": 9611,
        "THROTTLE_AUTH": 9711,
        "JNDI_PORT": 5672
    }