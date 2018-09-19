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

# importing required modules
import sys
from xml.etree import ElementTree as ET
import subprocess
import wget
import logging
import inspect
import os
import pymysql
import sqlparse
import stat
import re
from pathlib import Path
import urllib.request as urllib2
from xml.dom import minidom
from subprocess import Popen, PIPE
import configure_product as cp
from const import NS, TEST_PLAN_PROPERTY_FILE_NAME, LOG_FILE_NAME, DB_META_DATA, DIST_POM_PATH, \
    PRODUCT_STORAGE_DIR_NAME, DEFAULT_DB_USERNAME, ZIP_FILE_EXTENSION, SQL_DRIVER_PATH, START_SCRIPT_PATH, CRON_FILE, \
    NODE_TYPE_MASTER

lb_host = None
os_name = None
os_version = None
jdk = None
db_engine = None
db_host = None
db_port = None
db_username = None
db_password = None
db_engine_version = None
wum_username = None
wum_password = None
product_name = None
product_version = None
test_mode = None
node_count = None
node_type = None
offset = 0
product_git_branch = None
product_git_url = None
latest_product_release_api = None
latest_product_build_artifacts_api = None

database_config = {}

workspace = None
dist_name = None
dist_zip_name = None
product_id = None
log_file_name = None
target_path = None

tag_name = None
storage_dist_abs_path = None
storage_dir_abs_path = None

def read_proprty_files():
    global lb_host
    global os_name
    global os_version
    global jdk
    global db_engine
    global db_host
    global db_port
    global db_username
    global db_password
    global db_engine_version
    global wum_username
    global wum_password
    global product_name
    global product_version
    global test_mode
    global node_count
    global node_type
    global offset
    global product_git_branch
    global product_git_url
    global workspace
    global database_config
    global product_id
    global latest_product_release_api
    global latest_product_build_artifacts_api

    workspace = os.getcwd()
    property_file_paths = []
    test_plan_prop_path = Path(workspace, TEST_PLAN_PROPERTY_FILE_NAME)

    if Path.exists(test_plan_prop_path):
        property_file_paths.append(test_plan_prop_path)

        for path in property_file_paths:
            with open(path, 'r') as filehandle:
                for line in filehandle:
                    if line.startswith("#"):
                        continue
                    prop = line.split("=")
                    key = prop[0]
                    val = prop[1]
                    if key == "LBHost":
                        lb_host = val.strip()
                    elif key == "OS":
                        os_name = val.strip()
                    elif key == "OSVersion":
                        os_version = val.strip()
                    elif key == "JDK":
                        jdk = val.strip()
                    elif key == "DBEngine":
                        db_engine = val.strip()
                    elif key == "DBEngineVersion":
                        db_engine_version = val
                    elif key == "DBHost":
                        db_host = val.strip()
                    elif key == "DBPort":
                        db_port = val.strip()
                    elif key == "DBUsername":
                        db_username = val.strip()
                    elif key == "DBPassword":
                        db_password = val.strip()
                    elif key == "WUMUsername":
                        wum_username = val.strip()
                    elif key == "WUMPassword":
                        wum_password = val.strip()
                    elif key == "ProductName":
                        product_name = val.strip()
                    elif key == "ProductVersion":
                        product_version = val.strip()
                    elif key == "TestMode":
                        test_mode = val.strip()
                    elif key == "NodeCount":
                        node_count = int(val.strip())
                    elif key == "NodeType":
                        node_type = val.strip()
                    elif key == "Offset":
                        offset = int(val.strip())
                    elif key == "ProductGITBranch":
                        product_git_branch = val.strip()
                    elif key == "ProductGITURL":
                        product_git_url = val.strip()
                        product_id = product_git_url.split("/")[-1].split('.')[0]
                    elif key == "LATEST_PRODUCT_RELEASE_API":
                        latest_product_release_api = val.strip().replace('\\', '')
                    elif key == "LATEST_PRODUCT_BUILD_ARTIFACTS_API":
                        latest_product_build_artifacts_api = val.strip().replace('\\', '')
    else:
        raise Exception("Test Plan Property file is not in the workspace: " + workspace)

def validate_property_readings():
    missing_values = ""
    if lb_host is None:
        missing_values += " -LBHost- "
    if os_name is None:
        missing_values += " -OS- "
    if os_version is None:
        missing_values += " -OSVersion- "
    if jdk is None:
        missing_values += " -JDK- "
    if db_engine is None:
        missing_values += " -DBEngine- "
    if db_engine_version is None:
        missing_values += " -DBEngineVersion- "
    if db_host is None:
        missing_values += " -DBHost- "
    if db_port is None:
        missing_values += " -DBPort- "
    if db_username is None:
        missing_values += " -DBUsername- "
    if db_password is None:
        missing_values += " -DBPassword- "
    if wum_username is None:
        missing_values += " -WUMUserName- "
    if wum_password is None:
        missing_values += " -WUMPassword- "
    if product_name is None:
        missing_values += " -ProductName- "
    if product_version is None:
        missing_values += " -ProductVersion- "
    if test_mode is None:
        missing_values += " -TestMode- "
    if node_count is None:
        missing_values += " -NodeCount- "
    if node_type is None:
        missing_values += " -NodeType- "
    if product_git_url is None:
        missing_values += " -ProductGITURL- "
    if product_git_branch is None:
        missing_values += " -ProductGITBranch- "
    if latest_product_release_api is None:
        missing_values += " -LATEST_PRODUCT_RELEASE_API- "
    if latest_product_build_artifacts_api is None:
        missing_values += " -LATEST_PRODUCT_BUILD_ARTIFACTS_API- "
    if missing_values != "":
        logger.error('Invalid property file is found. Missing values: %s ', missing_values)
        return False
    else:
        return True

def download_file(url, destination):
    """Download a file using wget package.
    Download the given file in _url_ as the directory+name provided in _destination_
    """
    wget.download(url, destination)

def get_relative_path_of_dist_storage(xml_path):
    """Get the relative path of distribution storage
    """
    dom = minidom.parse(urllib2.urlopen(xml_path))  # parse the data
    artifact_elements = dom.getElementsByTagName('artifact')

    for artifact in artifact_elements:
        file_name_elements = artifact.getElementsByTagName("fileName")
        for file_name in file_name_elements:
            if file_name.firstChild.nodeValue == dist_zip_name:
                parent_node = file_name.parentNode
                return parent_node.getElementsByTagName("relativePath")[0].firstChild.nodeValue
    return None

def get_dist_name():
    """Get the product name by reading distribution pom.
    """
    global dist_name
    global dist_zip_name
    global product_version
    dist_pom_path = Path(workspace, product_id, DIST_POM_PATH[product_id])
    if sys.platform.startswith('win'):
        dist_pom_path = cp.winapi_path(dist_pom_path)
    ET.register_namespace('', NS['d'])
    artifact_tree = ET.parse(dist_pom_path)
    artifact_root = artifact_tree.getroot()
    parent = artifact_root.find('d:parent', NS)
    artifact_id = artifact_root.find('d:artifactId', NS).text
    product_version = parent.find('d:version', NS).text
    dist_name = artifact_id + "-" + product_version
    dist_zip_name = dist_name + ZIP_FILE_EXTENSION
    return dist_name

def get_latest_released_dist():
    """Get the latest released distribution
    """
    # construct the distribution downloading url
    relative_path = get_relative_path_of_dist_storage(latest_product_release_api + "xml")
    if relative_path is None:
        raise Exception("Error occured while getting relative path")
    dist_downl_url = latest_product_release_api.split('/api')[0] + "/artifact/" + relative_path
    # download the last released pack from Jenkins
    download_file(dist_downl_url, str(get_product_file_path()))
    logger.info('downloading the latest released pack from Jenkins is completed.')

def clone_repo():
    """Clone the product repo
    """
    try:
        subprocess.call(['git', 'clone', '--branch', product_git_branch, product_git_url], cwd=workspace)
        logger.info('product repository cloning is done.')
    except Exception as e:
        logger.error("Error occurred while cloning the product repo: ", exc_info=True)


def checkout_to_tag(name):
    """Checkout to the given tag
    """
    try:
        git_path = Path(workspace, product_id)
        tag = "tags/" + name
        subprocess.call(["git", "fetch", "origin", tag], cwd=git_path)
        subprocess.call(["git", "checkout", "-B", tag, name], cwd=git_path)
        logger.info('checkout to the branch: ' + tag)
    except Exception as e:
        logger.error("Error occurred while cloning the product repo and checkout to the latest tag of the branch",
                     exc_info=True)

def get_latest_tag_name(product):
    """Get the latest tag name from git location
    """
    global tag_name
    git_path = Path(workspace, product)
    binary_val_of_tag_name = subprocess.Popen(["git", "describe", "--abbrev=0", "--tags"], stdout=subprocess.PIPE, cwd=git_path)
    tag_name = binary_val_of_tag_name.stdout.read().strip().decode("utf-8")
    return tag_name

def get_latest_stable_artifacts_api():
    """Get the API of the latest stable artifacts
    """
    print(latest_product_build_artifacts_api)
    dom = minidom.parse(urllib2.urlopen(latest_product_build_artifacts_api + "xml"))
    main_artifact_elements = dom.getElementsByTagName('mainArtifact')
    for main_artifact in main_artifact_elements:
        canonical_name_elements = main_artifact.getElementsByTagName("canonicalName")
        for canonical_name in canonical_name_elements:
            if canonical_name.firstChild.nodeValue == dist_name + ".pom":
                parent_node = main_artifact.parentNode
                return parent_node.getElementsByTagName("url")[0].firstChild.nodeValue
    return None


def get_product_file_path():
    """Get the absolute path of the distribution which is located in the storage directory
    """
    global storage_dist_abs_path
    global storage_dir_abs_path
    # product download path and file name constructing
    storage_dir_abs_path = Path(workspace, PRODUCT_STORAGE_DIR_NAME)
    if not Path.exists(storage_dir_abs_path):
        Path(storage_dir_abs_path).mkdir(parents=True, exist_ok=True)
    storage_dist_abs_path = storage_dir_abs_path / dist_zip_name
    return storage_dist_abs_path

def get_latest_stable_dist():
    """Download the latest stable distribution
    """
    build_num_artifact = get_latest_stable_artifacts_api()
    print(build_num_artifact)
    build_num_artifact = re.sub(r'http.//(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d{1,5})', "https://wso2.org", build_num_artifact)
    if build_num_artifact is None:
        raise Exception("Error occured while getting latest stable build artifact API path")
    relative_path = get_relative_path_of_dist_storage(build_num_artifact + "api/xml")
    if relative_path is None:
        raise Exception("Error occured while getting relative path")
    dist_downl_url = build_num_artifact + "artifact/" + relative_path
    download_file(dist_downl_url, str(get_product_file_path()))
    logger.info('downloading the latest stable pack from Jenkins is completed.')

def function_logger(file_level, console_level=None):
    global log_file_name
    log_file_name = LOG_FILE_NAME
    function_name = inspect.stack()[1][3]
    logger = logging.getLogger(function_name)
    # By default, logs all messages
    logger.setLevel(logging.DEBUG)

    if console_level != None:
        # StreamHandler logs to console
        ch = logging.StreamHandler()
        ch.setLevel(console_level)
        ch_format = logging.Formatter('%(asctime)s - %(message)s')
        ch.setFormatter(ch_format)
        logger.addHandler(ch)

    # log in to a file
    fh = logging.FileHandler("{0}.log".format(function_name))
    fh.setLevel(file_level)
    fh_format = logging.Formatter('%(asctime)s - %(lineno)d - %(levelname)-8s - %(message)s')
    fh.setFormatter(fh_format)
    logger.addHandler(fh)
    return logger

def get_db_meta_data(argument):
    switcher = DB_META_DATA
    return switcher.get(argument, False)

def construct_url(prefix):
    url = prefix + db_host + ":" + db_port
    return url

def get_db_hostname(url, db_type):
    """Retreive db hostname from jdbc url
    """
    if db_type == 'ORACLE':
        hostname = url.split(':')[3].replace("@", "")
    else:
        hostname = url.split(':')[2].replace("//", "")
    return hostname


def run_sqlserver_commands(query):
    """Run SQL_SERVER commands using sqlcmd utility.
    """
    subprocess.call(
        ['sqlcmd', '-S', db_host, '-U', database_config['user'], '-P', database_config['password'], '-Q', query])


def get_mysql_connection(db_name=None):
    if db_name is not None:
        conn = pymysql.connect(host=get_db_hostname(database_config['url'], 'MYSQL'), user=database_config['user'],
                               passwd=database_config['password'], db=db_name)
    else:
        conn = pymysql.connect(host=get_db_hostname(database_config['url'], 'MYSQL'), user=database_config['user'],
                               passwd=database_config['password'])
    return conn


def run_mysql_commands(query):
    """Run mysql commands using mysql client when db name not provided.
    """
    conn = get_mysql_connection()
    conectr = conn.cursor()
    conectr.execute(query)
    conn.close()


def get_ora_user_carete_query(database):
    query = "CREATE USER {0} IDENTIFIED BY {1};".format(
        database, database_config["password"])
    return query


def get_ora_grant_query(database):
    query = "GRANT CONNECT, RESOURCE, DBA TO {0};".format(
        database)
    return query


def execute_oracle_command(query):
    """Run oracle commands using sqlplus client when db name(user) is not provided.
    """
    connect_string = "{0}/{1}@//{2}/{3}".format(database_config["user"], database_config["password"],
                                                db_host, "ORCL")
    session = Popen(['sqlplus', '-S', connect_string], stdin=PIPE, stdout=PIPE, stderr=PIPE)
    session.stdin.write(bytes(query, 'utf-8'))
    return session.communicate()


def create_oracle_user(database):
    """This method is able to create the user and grant permission to the created user in oracle
    """
    user_creating_query = get_ora_user_carete_query(database)
    logger.info(execute_oracle_command(user_creating_query))
    permission_granting_query = get_ora_grant_query(database)
    return execute_oracle_command(permission_granting_query)


def run_oracle_script(script, database):
    """Run oracle commands using sqlplus client when dbname(user) is provided.
    """
    connect_string = "{0}/{1}@//{2}/{3}".format(database, database_config["password"],
                                                db_host, "ORCL")
    session = Popen(['sqlplus', '-S', connect_string], stdin=PIPE, stdout=PIPE, stderr=PIPE)
    session.stdin.write(bytes(script, 'utf-8'))
    return session.communicate()


def run_sqlserver_script_file(db_name, script_path):
    """Run SQL_SERVER script file on a provided database.
    """
    subprocess.call(
        ['sqlcmd', '-S', db_host, '-U', database_config["user"], '-P', database_config["password"], '-d', db_name, '-i',
         script_path])


def run_mysql_script_file(db_name, script_path):
    """Run MYSQL db script file on a provided database.
    """
    conn = get_mysql_connection(db_name)
    connector = conn.cursor()
    sql = open(script_path).read()
    sql_parts = sqlparse.split(sql)
    for sql_part in sql_parts:
        if sql_part.strip() == '':
            continue
        connector.execute(sql_part)
    conn.close()

def setup_databases(db_names):
    """Create required databases.
    """
    base_path = Path(workspace, PRODUCT_STORAGE_DIR_NAME, dist_name, 'dbscripts')
    engine = db_engine.upper()
    db_meta_data = get_db_meta_data(engine)
    if db_meta_data:
        databases = db_meta_data["DB_SETUP"][product_id]
        if databases:
            for db_name in db_names:
                db_scripts = databases[db_name]
                if len(db_scripts) == 0:
                    if engine == 'SQLSERVER-SE':
                        # create database for MsSQL
                        run_sqlserver_commands('CREATE DATABASE {0}'.format(db_name))
                    elif engine == 'MYSQL':
                        # create database for MySQL
                        run_mysql_commands('CREATE DATABASE IF NOT EXISTS {0};'.format(db_name))
                    elif engine == 'ORACLE-SE2':
                        # create database for Oracle
                        create_oracle_user(db_name)
                else:
                    if engine == 'SQLSERVER-SE':
                        # create database for MsSQL
                        run_sqlserver_commands('CREATE DATABASE {0}'.format(db_name))
                        for db_script in db_scripts:
                            path = base_path / db_script
                            # run db scripts
                            run_sqlserver_script_file(db_name, str(path))
                    elif engine == 'MYSQL':
                        # create database for MySQL
                        run_mysql_commands('CREATE DATABASE IF NOT EXISTS {0};'.format(db_name))
                        # run db scripts
                        for db_script in db_scripts:
                            path = base_path / db_script
                            run_mysql_script_file(db_name, str(path))
                    elif engine == 'ORACLE-SE2':
                        # create oracle schema
                        create_oracle_user(db_name)
                        # run db script
                        for db_script in db_scripts:
                            path = base_path / db_script
                            run_oracle_script('@{0}'.format(str(path)), db_name)
            logger.info('Database setting up is done.')
        else:
            raise Exception("Database setup configuration is not defined in the constant file")
    else:
        raise Exception("Database meta data is not defined in the constant file")

def construct_db_config():
    """Use properties which are get by reading property files and construct the database config object which will use
    when configuring the databases.
    """
    db_meta_data = get_db_meta_data(db_engine.upper())
    if db_meta_data:
        database_config["driver_class_name"] = db_meta_data["driverClassName"]
        database_config["password"] = db_password
        database_config["sql_driver_location"] = SQL_DRIVER_PATH[os_name.upper()] + "/" + db_meta_data["jarName"]
        #database_config["sql_driver_location"] = "/Users/harshan/development/projects/wso2/testgrid/AWS/AMI-Configs/Oracle/ojdbc7.jar"
        database_config["url"] = construct_url(db_meta_data["prefix"])
        database_config["db_engine"] = db_engine
        if db_username is None:
            database_config["user"] = DEFAULT_DB_USERNAME
        else:
            database_config["user"] = db_username

    else:
        raise BaseException(
            "DB config parsing is failed. DB engine name in the property file doesn't match with the constant: " + str(
                db_engine.upper()))

def winapi_path(dos_path, encoding=None):
    path = os.path.abspath(dos_path)

    if path.startswith("\\\\"):
        path = "\\\\?\\UNC\\" + path[2:]
    else:
        path = "\\\\?\\" + path

    return path

def start_product():
    if sys.platform.startswith('win'):
        script_path = winapi_path(Path(storage_dir_abs_path, dist_name, START_SCRIPT_PATH[product_id]['WINDOWS']))
        subprocess.call([script_path, 'start'])
    else:
        script_path = Path(storage_dir_abs_path, dist_name, START_SCRIPT_PATH[product_id]['CENTOS'])
        subprocess.call(['sh', script_path, 'start'])

def start_rsync():
    logger.info('RSync starting ....')
    if sys.platform.startswith('win'):
        crontab_filepath = Path(workspace, CRON_FILE)
        subprocess.call([crontab_filepath, 'start'])
    else:
        crontab_filepath = Path(workspace, CRON_FILE)
        if Path.exists(crontab_filepath):
            os.chmod(crontab_filepath, stat.S_IRUSR | stat.S_IXUSR | stat.S_IRGRP | stat.S_IXGRP)
            rsync_command = "nohup sh " + str(crontab_filepath) + " &"
            subprocess.Popen(rsync_command, shell=True,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        else:
            raise FileNotFoundError("cron-job.sh script is not found, file path: " + str(crontab_filepath))

def main():
    try:
        global logger
        global dist_name
        global storage_dir_abs_path

        logger = function_logger(logging.DEBUG, logging.DEBUG)
        if sys.version_info < (3, 6):
            raise Exception(
                "To run deploy.py script you must have Python 3.6 or latest. Current version info: " + sys.version_info)
        read_proprty_files()
        if not validate_property_readings():
            raise Exception(
                "Property file doesn't have mandatory key-value pair. Please verify the content of the property file "
                "and the format")
        # construct database configuration
        construct_db_config()

        # clone repo
        clone_repo()

        if test_mode == "DEBUG":
            checkout_to_tag(get_latest_tag_name(product_id))
            dist_name = get_dist_name()
            get_latest_released_dist()
        elif test_mode == "RELEASE":
            checkout_to_tag(get_latest_tag_name(product_id))
            dist_name = get_dist_name()
            get_latest_released_dist()
        elif test_mode == "SNAPSHOT":
            dist_name = get_dist_name()
            get_latest_stable_dist()
            get_product_file_path()
        elif test_mode == "WUM":
            # todo after identify specific steps that are related to WUM, add them to here
            dist_name = get_dist_name()
            logger.info("WUM specific steps are empty")

        db_names = cp.configure_product(dist_name, product_id, database_config, workspace, product_version, os_name,
                                        lb_host, node_count, node_type, offset)
        if db_names is None or not db_names:
            raise Exception("Failed the product configuring")

        if node_type == NODE_TYPE_MASTER:
            setup_databases(db_names)
            start_rsync()
        start_product()
    except Exception as e:
        logger.error("Error occurred while running the deploy.py script", exc_info=True)
    except BaseException as e:
        logger.error("Error occurred while doing the configuration", exc_info=True)


if __name__ == "__main__":
    main()