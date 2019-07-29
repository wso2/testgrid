--
-- Table structure for table `infrastructure_parameter`
--

DROP TABLE IF EXISTS `infrastructure_parameter`;

CREATE TABLE `infrastructure_parameter` (
  `id` varchar(36) NOT NULL,
  `created_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `NAME` varchar(255) DEFAULT NULL,
  `PROPERTIES` varchar(255) DEFAULT NULL,
  `ready_for_testgrid` tinyint(1) DEFAULT '0',
  `TYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_infrastructure_parameter_0` (`NAME`,`PROPERTIES`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `infrastructure_parameter` WRITE;

INSERT INTO `infrastructure_parameter` VALUES 
('1','2018-02-01 10:41:08','2018-11-27 17:34:23','OracleJDK-8','JDK=ORACLE_JDK8',1,'JDK'),
('1bd03ead-84ff-4bfc-b696-8026d8849c8','2018-03-20 06:32:16','2018-12-07 09:55:16','MySQL-5.6','DBEngineVersion=5.6\nDB=MySQL-5.6\nDBEngine=mysql',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b696-8036d8849c8','2019-05-31 13:56:50','2019-05-31 13:56:50','MySQL-8.0','DBEngineVersion=8.0\nDB=MySQL-8.0\nDBEngine=mysql',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b746-8226d9949c6','2019-02-28 03:55:54','2019-03-01 05:34:13','CorrettoJDK-8','JDK=CORRETTO_JDK8',1,'JDK'),
('1bd03ead-84ff-4bfc-b796-8026d8349c8','2018-03-20 06:32:28','2018-11-27 17:32:34','MySQL-5.7','DBEngineVersion=5.7\nDB=MySQL-5.7\nDBEngine=mysql',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b796-8026d8849c3','2018-03-14 01:27:01','2018-07-13 07:41:50','OPEN_JDK9','{}',0,'JDK'),
('1bd03ead-84ff-4bfc-b796-8026d8849c6','2018-03-12 06:13:12','2019-04-17 12:45:00','Ubuntu-18.04','OS=Ubuntu\nOSVersion=18.04\nOperatingSystem=Ubuntu1804',1,'OS'),
('1bd03ead-84ff-4bfc-b796-8026d8849c7','2018-03-12 06:15:17','2018-12-07 08:28:02','CentOS-7.4','OSVersion=7.4\nOS=CentOS\nOperatingSystem=CentOS7.4',0,'OS'),
('1bd03ead-84ff-4bfc-b796-8026d8849c8','2018-03-12 06:22:06','2018-07-13 07:42:43','ORACLE_JDK9','{}',0,'JDK'),
('1bd03ead-84ff-4bfc-b796-8026d8849c9','2018-03-14 01:26:34','2019-01-30 12:20:45','OPEN_JDK8','JDK=OPEN_JDK8',1,'JDK'),
('1bd03ead-84ff-4bfc-b796-8026d8849d2','2018-03-19 12:43:54','2018-12-06 14:11:01','SQLServer-SE-13.00','DBEngineVersion=13.00\nDB=SQLServer-SE-13.00\nDBEngine=sqlserver-se',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b796-8026d8849g7','2018-03-12 06:15:17','2018-12-07 09:27:52','RHEL-7.0','OSVersion=7.0 OS=RHEL_7.0',0,'OS'),
('1bd03ead-84ff-4bfc-b796-8026d8849h7','2018-03-12 06:15:17','2019-04-17 13:37:59','RHEL-7.4','OSVersion=7.4\nOS=RHEL\nOperatingSystem=RHEL74',1,'OS'),
('1bd03ead-84ff-4bfc-b796-8026d8849h9','2019-01-08 09:43:42','2019-01-08 09:43:42','ADOPT_OPEN_JDK8','JDK=ADOPT_OPEN_JDK8',1,'JDK'),
('1bd03ead-84ff-4bfc-b796-8026d9849p7','2018-03-12 06:15:17','2019-01-30 12:22:15','CentOS-7.5','OSVersion=7.5\nOS=CentOS\nOperatingSystem=CentOS7',1,'OS'),
('1bd03ead-84ff-4bfc-b796-8426d8849d2','2018-03-19 12:43:54','2018-12-06 14:11:01','SQLServer-SE-14.00','DBEngineVersion=14.00\nDB=SQLServer-SE-14.00\nDBEngine=sqlserver-se',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b796-8726d834567','2018-07-13 07:56:48','2018-12-07 09:29:22','Windows-2016','OSVersion=2016\nOS=Windows\nOperatingSystem=Windows2016',1,'OS'),
('1bd03ead-84ff-4bfc-b796-8726d834568','2019-03-13 07:56:48','2019-03-13 07:56:48','SUSE-Linux-12','OSVersion=12\nOS=SUSE\nOperatingSystem=SUSE12',1,'OS'),
('1bd03ead-84ff-4bfc-b796-8726d834569','2019-03-13 07:56:48','2019-03-13 07:56:48','Postgres-9.4.12','DBEngineVersion=9.4.12\nDB=Postgres-9.4.12\nDBEngine=postgres',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b796-8726d88567d3','2018-07-13 07:52:43','2018-12-12 11:57:56','Oracle-SE2-12.1','DBEngineVersion=12.1.0.2.v12\nDBEngine=oracle-se2\nDB=Oracle-SE2-12.1',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b796-8726d934567','2019-04-17 13:37:21','2019-04-17 13:37:21','RHEL-7.6','OSVersion=7.6\nOS=RHEL\nOperatingSystem=RHEL76',1,'OS'),
('1bd03ead-84ff-4bfc-b796-9726d834569','2019-03-13 07:56:48','2019-03-13 07:56:48','Postgres-9.4','DBEngineVersion=9.4\nDB=Postgres-9.4\nDBEngine=postgres',1,'DBEngine'),
('1bd03ead-84ff-4bfc-b896-8026d8849c3','2018-02-13 01:46:49','2018-12-07 12:35:51','Postgres-10.3','DBEngineVersion=10.3\nDB=postgres-10.3\nDBEngine=postgres',1,'DBEngine'),
('1bd03ead-84ff-4cfc-b796-8025d8849c8','2018-03-23 05:23:01','2018-12-12 11:43:22','Postgres-10.5','DBEngineVersion=10.5\nDB=Postgres-10.5\nDBEngine=postgres',1,'DBEngine'),
('1bd03ead-84ff-4dfc-b796-8025d8849d9','2018-03-23 05:23:31','2018-12-17 07:25:13','Postgres-9.6','DBEngineVersion=9.6\nDB=Postgres-9.6\nDBEngine=postgres',1,'DBEngine'),
('1be03ead-84ff-4bfc-b696-8026d8849e0','2018-12-12 11:55:09','2019-04-09 12:54:26','Oracle-SE2-11.1','DBEngineVersion=11.2\nDBEngine=oracle-se1\nDB=Oracle-SE1-11.2',1,'DBEngine'),
('1be03ead-84ff-4bfc-b696-8026d8850c8','2019-04-08 13:12:03','2019-04-09 12:54:26','Oracle-SE1-11.2','DBEngineVersion=11.2\nDBEngine=oracle-se1\nDB=Oracle-SE1-11.2',1,'DBEngine');

UNLOCK TABLES;

