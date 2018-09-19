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
from xml.etree import ElementTree as ET
from zipfile import ZipFile
import os
import stat
import sys
from pathlib import Path
import shutil
import logging
import copy
import json
from const import ZIP_FILE_EXTENSION, NS, CARBON_NAME, VALUE_TAG, OS_USER_DATA, NODE_IP_PREFIX, SYNC_DIRS, \
    DEFAULT_ORACLE_SID, DATASOURCE_PATHS, MYSQL_DB_ENGINE, ORACLE_DB_ENGINE, LIB_PATH, PRODUCT_STORAGE_DIR_NAME, \
    MSSQL_DB_ENGINE, WORKER_LIST_FILENAME, RSYNC_CRON_SCRIPT_TEMPLATE, RSYNC_CRON_SCRIPT_FILE, CONFIG_PATHS, \
    TYPE_RECEIVER, TYPE_AUTH, STATIC_CONFIGS_DIR, PORT_NUMBERS, NODE_TYPE_MASTER

datasource_paths = None
database_url = None
database_user = None
database_pwd = None
database_drive_class_name = None
dist_name = None
storage_dist_abs_path = None
target_dir_abs_path = None
database_config = None
storage_dir_abs_path = None
storage_zip_abs_path = None
workspace = None
sql_driver_location = None
product_id = None
database_names = []
os_name = None
host_name = None
node_type = None
node_count = None
offset = None
conf_paths = None

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)


class ZipFileLongPaths(ZipFile):
    def _extract_member(self, member, targetpath, pwd):
        targetpath = winapi_path(targetpath)
        return ZipFile._extract_member(self, member, targetpath, pwd)


def winapi_path(dos_path, encoding=None):
    path = os.path.abspath(dos_path)

    if path.startswith("\\\\"):
        path = "\\\\?\\UNC\\" + path[2:]
    else:
        path = "\\\\?\\" + path

    return path


def on_rm_error(func, path, exc_info):
    os.chmod(path, stat.S_IWRITE)
    os.unlink(path)

def extract_product():
    """Extract the zip file(product zip) which is located in the given @path.
    """
    if Path.exists(storage_zip_abs_path):
        logger.info("Extracting the product  into " + str(storage_dir_abs_path))
        if sys.platform.startswith('win'):
            with ZipFileLongPaths(storage_zip_abs_path, "r") as zip_ref:
                zip_ref.extractall(storage_dir_abs_path)
        else:
            with ZipFile(str(storage_zip_abs_path), "r") as zip_ref:
                zip_ref.extractall(storage_dir_abs_path)
    else:
        raise FileNotFoundError("File is not found to extract, file path: " + str(storage_zip_abs_path))

def get_rsync_root_path(nodeId, dir):
    node_ip = NODE_IP_PREFIX + str((nodeId - 1) * 10)
    user_name = OS_USER_DATA["CENTOS"]
    return user_name + "@" + node_ip + ":" + get_abs_path(dir)

def get_rsync_command(dir, node_id):
    if sys.platform.startswith('win'):
        return 'rsync --delete -arve "ssh -i $pem_file -o StrictHostKeyChecking=no" ' + get_abs_path(dir) + ' ' + \
               get_rsync_root_path(node_id, dir) + ' >> /tmp/carbon-rsync-logs/logs.txt \n'
    else:
        return 'rsync --delete -arve "ssh -i $pem_file -o StrictHostKeyChecking=no" ' + get_abs_path(dir) + ' ' + \
               get_rsync_root_path(node_id, dir) + ' >> /tmp/carbon-rsync-logs/logs.txt \n'

def get_abs_path(dir):
    return str(storage_dir_abs_path) + "/" + dist_name + dir

def configure_rsync():
    logger.info("Configuring rsync files")
    sync_dirs = SYNC_DIRS[product_id]
    cron_script_template_path = Path(workspace, RSYNC_CRON_SCRIPT_TEMPLATE[os_name.upper()])

    if sys.platform.startswith('win'):
        cron_script_template_path = winapi_path(cron_script_template_path)

    with open(cron_script_template_path, "a") as fin:
        for nodeId in range(2, node_count + 1):
            for dir in sync_dirs:
                fin.write(get_rsync_command(dir, nodeId))
    os.chmod(cron_script_template_path, stat.S_IRUSR | stat.S_IXUSR | stat.S_IRGRP | stat.S_IXGRP | stat.S_IROTH |
             stat.S_IXOTH)

def copy_file(source, destination):
    """Copy files from source to destination.
    """
    if sys.platform.startswith('win'):
        source = winapi_path(source)
        destination = winapi_path(destination)
    shutil.copy(source, destination)

def add_additional_datasources(data_sources):
    logger.info("Adding WSO2REG_DB and WSO2UM_DB datasources to master-datasources.xml")
    data_source = data_sources.find('datasource')
    reg_data_source = copy.deepcopy(data_source)
    um_data_source = copy.deepcopy(data_source)
    for child in reg_data_source:
        if child.tag == 'name':
            child.text = 'WSO2REG_DB'
        break
    reg_jndi_name = reg_data_source.find('jndiConfig/name')
    reg_jndi_name.text = 'jdbc/WSO2REG_DB'

    for child in um_data_source:
        if child.tag == 'name':
            child.text = 'WSO2UM_DB'
        break
    um_jndi_name = um_data_source.find('jndiConfig/name')
    um_jndi_name.text = 'jdbc/WSO2UM_DB'

    data_sources.append(reg_data_source)
    data_sources.append(um_data_source)
    return data_sources

def copy_static_configs():
    logger.info("Copying registry.xml & user-mgt.xml files")
    reg_source_file_path = Path(STATIC_CONFIGS_DIR, 'registry.xml')
    umgt_source_file_path = Path(STATIC_CONFIGS_DIR, 'user-mgt.xml')
    reg_dest_file_path = Path(storage_dist_abs_path, conf_paths['REGISTRY_CONF'])
    umgt_dest_file_path = Path(storage_dist_abs_path, conf_paths['USER_CONF'])
    if sys.platform.startswith('win'):
        reg_dest_file_path = winapi_path(reg_dest_file_path)
        reg_source_file_path = winapi_path(reg_source_file_path)
        umgt_source_file_path = winapi_path(umgt_source_file_path)
        umgt_dest_file_path = winapi_path(umgt_dest_file_path)

    if Path.exists(reg_source_file_path):
        copy_file(reg_source_file_path, reg_dest_file_path)
    else:
        raise FileNotFoundError("registry.xml file is not found to copy, file path: " + str(reg_source_file_path))

    if Path.exists(umgt_source_file_path):
        copy_file(umgt_source_file_path, umgt_dest_file_path)
    else:
        raise FileNotFoundError("user-mgt.xml file is not found to copy, file path: " + str(umgt_source_file_path))

def modify_datasources():
    """Modify datasources files which are defined in the const.py. DB ulr, uname, pwd, driver class values are modifying.
    """
    logger.info("Configuring datasources")
    for data_source in datasource_paths:
        file_path = Path(storage_dist_abs_path, data_source)
        if sys.platform.startswith('win'):
            file_path = winapi_path(file_path)
        logger.info("Modifying datasource: " + str(file_path))
        artifact_tree = ET.parse(file_path)
        artifarc_root = artifact_tree.getroot()
        data_sources = artifarc_root.find('datasources')
        if "master-datasources" in data_source:
            data_sources = add_additional_datasources(data_sources)
        for item in data_sources.findall('datasource'):
            database_name = None
            for child in item:
                if child.tag == 'name':
                    # Skip configuring WSO2_CARBON_DB
                    if child.text == 'WSO2_CARBON_DB':
                        continue
                    else:
                        database_name = child.text
                # special checking for namespace object content:media
                if child.tag == 'definition' and database_name:
                    configuration = child.find('configuration')
                    url = configuration.find('url')
                    user = configuration.find('username')
                    password = configuration.find('password')
                    validation_query = configuration.find('validationQuery')
                    drive_class_name = configuration.find('driverClassName')
                    if MYSQL_DB_ENGINE == database_config['db_engine'].upper():
                        url.text = url.text.replace(url.text, database_config[
                            'url'] + "/" + database_name + "?autoReconnect=true&useSSL=false&requireSSL=false&"
                                                     "verifyServerCertificate=false")
                        user.text = user.text.replace(user.text, database_config['user'])
                    elif ORACLE_DB_ENGINE == database_config['db_engine'].upper():
                        url.text = url.text.replace(url.text, database_config['url'] + "/" + DEFAULT_ORACLE_SID)
                        user.text = user.text.replace(user.text, database_name)
                        validation_query.text = validation_query.text.replace(validation_query.text,
                                                                              "SELECT 1 FROM DUAL")
                    elif MSSQL_DB_ENGINE == database_config['db_engine'].upper():
                        url.text = url.text.replace(url.text,
                                                    database_config['url'] + ";" + "databaseName=" + database_name)
                        user.text = user.text.replace(user.text, database_config['user'])
                    else:
                        url.text = url.text.replace(url.text, database_config['url'] + "/" + database_name)
                        user.text = user.text.replace(user.text, database_config['user'])
                    password.text = password.text.replace(password.text, database_config['password'])
                    drive_class_name.text = drive_class_name.text.replace(drive_class_name.text,
                                                                          database_config['driver_class_name'])
                    database_names.append(database_name)
        artifact_tree.write(file_path)

def get_throttling_urls(type):
    node_str = None
    if type == TYPE_RECEIVER:
        node_str = '<ReceiverUrlGroup>'
        for nodeId in range(1, node_count + 1):
            if nodeId == 1:
                node_str = node_str + '{tcp://' + NODE_IP_PREFIX  + '00:' + \
                           str(get_port_value(PORT_NUMBERS['THROTTLE_PUBLISHER'])) + '},'
            else:
                node_str = node_str + '{tcp://' + NODE_IP_PREFIX + str((nodeId - 1) * 10) + ':' + \
                           str(get_port_value(PORT_NUMBERS['THROTTLE_PUBLISHER'])) + '},'
        node_str = node_str[0:-1]
        node_str += '</ReceiverUrlGroup>'
    elif type == TYPE_AUTH:
        node_str = '<AuthUrlGroup>'
        for nodeId in range(1, node_count + 1):
            if nodeId == 1:
                node_str = node_str + '{ssl://' + NODE_IP_PREFIX + '00:' + \
                           str(get_port_value(PORT_NUMBERS['THROTTLE_AUTH'])) + '},'
            else:
                node_str = node_str + '{ssl://' + NODE_IP_PREFIX + str((nodeId - 1) * 10) + ':' + \
                           str(get_port_value(PORT_NUMBERS['THROTTLE_AUTH'])) + '},'
        node_str = node_str[0:-1]
        node_str += '</AuthUrlGroup>'
    return node_str

def get_port_value(port):
    return offset + port

def configure_apim_xml():
    logger.info("Configuring api-manager.xml file")
    file_path = Path(storage_dist_abs_path, conf_paths['APIM_CONF'])
    if sys.platform.startswith('win'):
        file_path = winapi_path(file_path)

    if Path.exists(file_path):
        logger.info("Modifying api-manager.xml: " + str(file_path))
        artifact_tree = ET.parse(file_path)
        artifact_root = artifact_tree.getroot()
        # configure gateway server
        if node_type != 'MASTER':
            gateway_server_config = artifact_root.find('APIGateway/Environments/Environment/ServerURL')
            gateway_server_config.text = 'https://' + NODE_IP_PREFIX + '00:' + \
                                         str(get_port_value(PORT_NUMBERS['MGT_HTTPS'])) + '/services/'
        # configure gateway endpoints
        gateway_endpoints_config = artifact_root.find('APIGateway/Environments/Environment/GatewayEndpoint')
        gateway_endpoints_config.text = 'http://' + host_name + ':' + str(get_port_value(PORT_NUMBERS['NIO_HTTP'])) + \
                                        ',https://' + host_name + ':' + str(get_port_value(PORT_NUMBERS['NIO_HTTPS']))
        # configure APIStore urls
        api_store_url_config = artifact_root.find('APIStore/URL')
        api_store_url_config.text = host_name
        # configure Throttling
        throttling_data_publisher_config = artifact_root.find('ThrottlingConfigurations/DataPublisher')
        throttling_data_publisher_config.append(ET.fromstring(get_throttling_urls(TYPE_RECEIVER)))
        throttling_data_publisher_config.append(ET.fromstring(get_throttling_urls(TYPE_AUTH)))

        artifact_tree.write(file_path)
    else:
        raise FileNotFoundError("api-manager.xml file is not found to configure, file path: " + str(file_path))

def configure_carbon_xml():
    logger.info("Configuring carbon.xml file")
    ns = {'carbon': 'http://wso2.org/projects/carbon/carbon.xml'}
    ET.register_namespace('carbon', NS['carbon'])
    file_path = Path(storage_dist_abs_path, conf_paths['CARBON_CONF'])
    if sys.platform.startswith('win'):
        file_path = winapi_path(file_path)
    if Path.exists(file_path):
        logger.info("Modifying carbon.xml: " + str(file_path))
        artifact_tree = ET.parse(file_path)
        artifact_root = artifact_tree.getroot()
        # offset config
        port_offset = artifact_root.find('carbon:Ports/carbon:Offset', ns)
        port_offset.text = str(offset)
        # host configs
        artifact_root.append(ET.fromstring('<HostName>' + host_name + '</HostName>'))
        artifact_root.append(ET.fromstring('<MgtHostName>' + host_name + '</MgtHostName>'))
        artifact_tree.write(file_path)
    else:
        raise FileNotFoundError("carbon.xml file is not found to configure, file path: " + str(file_path))

def configure_jndi_properties():
    jndi_properties_filepath = Path(storage_dist_abs_path, conf_paths['JNDI_CONF'])
    logger.info("Configuring jndi.properties file : " + str(jndi_properties_filepath))
    jndi_port = str(get_port_value(PORT_NUMBERS['JNDI_PORT']))
    logger.info("port no :" + jndi_port)

    with open(jndi_properties_filepath) as f:
        s = f.read()
        if str(PORT_NUMBERS['JNDI_PORT']) not in s:
            return
    with open(jndi_properties_filepath, 'w') as f:
        s = s.replace(str(PORT_NUMBERS['JNDI_PORT']), jndi_port)
        f.write(s)

def configure_apps():
    logger.info("Configuring site.json files of store and publisher apps")
    static_conf_dir = Path(STATIC_CONFIGS_DIR, product_id)
    store_conf_path = Path(storage_dist_abs_path, conf_paths['STORE_CONF'])
    store_static_conf_path = Path( static_conf_dir, 'store_site.json')
    publisher_conf_path = Path(storage_dist_abs_path, conf_paths['PUBLISHER_CONF'])
    publisher_static_conf_path = Path(static_conf_dir, 'publisher_site.json')
    if sys.platform.startswith('win'):
        store_conf_path = winapi_path(store_conf_path)
        store_static_conf_path = winapi_path(store_static_conf_path)
        publisher_conf_path = winapi_path(publisher_conf_path)
        publisher_static_conf_path = winapi_path(publisher_static_conf_path)
    if Path.exists(store_static_conf_path):
        with open(store_static_conf_path, 'r') as f1:
            store_conf = json.load(f1)
            store_conf["reverseProxy"]["host"] = host_name
            store_conf["whiteListedHostNames"].append(host_name)
            with open(store_conf_path, 'w') as outfile:
                json.dump(store_conf, outfile, indent=4)
    else:
        raise FileNotFoundError("Store site.json file is not found to configure, file path: " + str(store_static_conf_path))

    if Path.exists(publisher_static_conf_path):
        with open(publisher_static_conf_path, 'r') as f2:
            publisher_conf = json.load(f2)
            publisher_conf["reverseProxy"]["host"] = host_name
            with open(publisher_conf_path, 'w') as outfile:
                json.dump(publisher_conf, outfile, indent=4)
    else:
        raise FileNotFoundError("Publisher site.json file is not found to configure, file path: " + str(publisher_static_conf_path))


def configure_product(name, id, db_config, ws, product_version, os_name1, host_name1, node_count1, node_type1, offset1):
    try:
        global dist_name
        global product_id
        global database_config
        global workspace
        global datasource_paths
        global storage_dist_abs_path
        global storage_dir_abs_path
        global storage_zip_abs_path
        global os_name
        global host_name
        global node_type
        global node_count
        global offset
        global conf_paths

        dist_name = name
        product_id = id
        database_config = db_config
        workspace = ws
        os_name = os_name1
        host_name = host_name1
        node_type = node_type1
        node_count = node_count1
        offset = offset1
        datasource_paths = DATASOURCE_PATHS[product_id]
        conf_paths = CONFIG_PATHS[product_id]
        zip_name = dist_name + ZIP_FILE_EXTENSION

        storage_dir_abs_path = Path(workspace, PRODUCT_STORAGE_DIR_NAME)
        storage_zip_abs_path = Path(storage_dir_abs_path, zip_name)
        storage_dist_abs_path = Path(storage_dir_abs_path, dist_name)

        extract_product()

        configure_carbon_xml()

        configure_jndi_properties()
        # Copy JDBC driver
        copy_file(Path(database_config['sql_driver_location']), Path(storage_dist_abs_path, LIB_PATH[product_id]))

        if datasource_paths is not None:
            modify_datasources()
        else:
            logger.info("datasource paths are not defined in the config file")

        copy_static_configs()

        configure_apim_xml()

        configure_apps()

        # configure rsync
        if node_type == NODE_TYPE_MASTER:
            configure_rsync()

        #os.remove(str(storage_zip_abs_path))
        return database_names
    except FileNotFoundError as e:
        logger.error("Error occurred while finding files", exc_info=True)
    except IOError as e:
        logger.error("Error occurred while accessing files", exc_info=True)
    except Exception as e:
        logger.error("Error occurred while configuring the product", exc_info=True)
