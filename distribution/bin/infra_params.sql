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

INSERT INTO `infrastructure_parameter` VALUES ('12d03ead-84ff-4bfc-b796-8016d8849c8','2018-02-01 10:41:08','2018-07-13 07:38:24','ORACLE_JDK8','{}',1,'JDK'),('12d03ead-84ff-4bfc-b796-8016d8849c9','2018-03-20 06:33:26','2018-07-13 07:38:59','mariadb','DBEngineVersion=10.0',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8024d8849d0','2018-03-20 06:33:34','2018-07-13 07:39:17','mariadb','DBEngineVersion=10.0.32',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8026d8849d2','2018-03-20 06:34:01','2018-05-25 14:24:39','oracle-se','DBEngineVersion=11.2',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8426d8849d1','2018-03-20 06:33:51','2018-07-17 12:18:22','oracle-se','DBEngineVersion=12.1',0,'DBEngine'),('1bd03ead-84ff-4bfc-b696-8026d8849c8','2018-03-20 06:32:16','2018-11-21 03:25:11','mysql','DBEngineVersion=5.6\nDB=MySQL-5.6',0,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8349c8','2018-03-20 06:32:28','2018-11-21 02:43:55','mysql','DBEngineVersion=5.7\nDB=MySQL-5.7',1,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8849c3','2018-03-14 01:27:01','2018-07-13 07:41:50','OPEN_JDK9','{}',0,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849c6','2018-03-12 06:13:12','2018-11-21 09:09:12','UBUNTU','OS=Ubuntu1804\nOSVersion=Ubuntu1804',1,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849c7','2018-03-12 06:15:17','2018-11-19 04:45:23','CentOS','OSVersion=7.4',0,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849c8','2018-03-12 06:22:06','2018-07-13 07:42:43','ORACLE_JDK9','{}',0,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849c9','2018-03-14 01:26:34','2018-07-17 10:50:29','OPEN_JDK8','{}',1,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849d2','2018-03-19 12:43:54','2018-07-17 10:50:29','sqlserver-se','DBEngineVersion=13.00',1,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8849g7','2018-03-12 06:15:17','2018-11-21 05:24:23','RHEL','OSVersion=7.0',1,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849h7','2018-03-12 06:15:17','2018-11-19 10:34:35','RHEL_7.2','OSVersion=7.2',0,'OS'),('1bd03ead-84ff-4bfc-b796-8026d9849p7','2018-03-12 06:15:17','2018-11-19 10:34:35','CentOS','OSVersion=7.5',1,'OS'),('1bd03ead-84ff-4bfc-b796-8726d834567','2018-07-13 07:56:48','2018-11-19 04:29:14','Windows','OSVersion=2016',0,'OS'),('1bd03ead-84ff-4bfc-b796-8726d88567d3','2018-07-13 07:52:43','2018-07-17 12:18:47','oracle-se2','DBEngineVersion=12.1.0.2.v12',1,'DBEngine'),('1bd03ead-84ff-4bfc-b896-8026d8849c3','2018-02-13 01:46:49','2018-07-13 07:48:15','postgres','DBEngineVersion=10.3',0,'DBEngine'),('1bd03ead-84ff-4cfc-b796-8025d8849c8','2018-03-23 05:23:01','2018-07-13 07:49:43','postgres','DBEngineVersion=10.1',0,'DBEngine'),('1bd03ead-84ff-4dfc-b796-8025d8849c8','2018-03-23 05:23:31','2018-07-13 07:49:59','postgres','DBEngineVersion=9.6.1',0,'DBEngine');

UNLOCK TABLES;

