-- MySQL dump 10.13  Distrib 5.7.24, for Linux (x86_64)
--
-- Host: localhost    Database: testgriddb
-- ------------------------------------------------------
-- Server version	5.7.24-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `infrastructure_parameter`
--

DROP TABLE IF EXISTS `infrastructure_parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `infrastructure_parameter`
--

LOCK TABLES `infrastructure_parameter` WRITE;
/*!40000 ALTER TABLE `infrastructure_parameter` DISABLE KEYS */;
INSERT INTO `infrastructure_parameter` VALUES ('12d03ead-84ff-4bfc-b796-8016d8849c8','2018-02-01 10:41:08','2018-07-13 07:38:24','ORACLE_JDK8','{}',1,'JDK'),('12d03ead-84ff-4bfc-b796-8016d8849c9','2018-03-20 06:33:26','2018-07-13 07:38:59','mariadb','DBEngineVersion=10.0',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8024d8849d0','2018-03-20 06:33:34','2018-07-13 07:39:17','mariadb','DBEngineVersion=10.0.32',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8026d8849d2','2018-03-20 06:34:01','2018-05-25 14:24:39','oracle-se','DBEngineVersion=11.2',0,'DBEngine'),('12d03ead-84ff-4bfc-b796-8426d8849d1','2018-03-20 06:33:51','2018-07-17 12:18:22','oracle-se','DBEngineVersion=12.1',0,'DBEngine'),('1bd03ead-84ff-4bfc-b696-8026d8849c8','2018-03-20 06:32:16','2018-11-21 03:25:11','mysql','DBEngineVersion=5.6\nDB=MySQL-5.6',0,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8349c8','2018-03-20 06:32:28','2018-11-21 02:43:55','mysql','DBEngineVersion=5.7\nDB=MySQL-5.7',1,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8849c3','2018-03-14 01:27:01','2018-07-13 07:41:50','OPEN_JDK9','{}',0,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849c6','2018-03-12 06:13:12','2018-11-21 09:09:12','UBUNTU','OS=Ubuntu1804\nOSVersion=Ubuntu1804',1,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849c7','2018-03-12 06:15:17','2018-11-19 04:45:23','CentOS','OSVersion=7.4',0,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849c8','2018-03-12 06:22:06','2018-07-13 07:42:43','ORACLE_JDK9','{}',0,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849c9','2018-03-14 01:26:34','2018-07-17 10:50:29','OPEN_JDK8','{}',1,'JDK'),('1bd03ead-84ff-4bfc-b796-8026d8849d2','2018-03-19 12:43:54','2018-07-17 10:50:29','sqlserver-se','DBEngineVersion=13.00',1,'DBEngine'),('1bd03ead-84ff-4bfc-b796-8026d8849g7','2018-03-12 06:15:17','2018-11-21 05:24:23','RHEL','OSVersion=7.0',1,'OS'),('1bd03ead-84ff-4bfc-b796-8026d8849h7','2018-03-12 06:15:17','2018-11-19 10:34:35','RHEL_7.2','OSVersion=7.2',0,'OS'),('1bd03ead-84ff-4bfc-b796-8026d9849p7','2018-03-12 06:15:17','2018-11-19 10:34:35','CentOS','OSVersion=7.5',1,'OS'),('1bd03ead-84ff-4bfc-b796-8726d834567','2018-07-13 07:56:48','2018-11-19 04:29:14','Windows','OSVersion=2016',0,'OS'),('1bd03ead-84ff-4bfc-b796-8726d88567d3','2018-07-13 07:52:43','2018-07-17 12:18:47','oracle-se2','DBEngineVersion=12.1.0.2.v12',1,'DBEngine'),('1bd03ead-84ff-4bfc-b896-8026d8849c3','2018-02-13 01:46:49','2018-07-13 07:48:15','postgres','DBEngineVersion=10.3',0,'DBEngine'),('1bd03ead-84ff-4cfc-b796-8025d8849c8','2018-03-23 05:23:01','2018-07-13 07:49:43','postgres','DBEngineVersion=10.1',0,'DBEngine'),('1bd03ead-84ff-4dfc-b796-8025d8849c8','2018-03-23 05:23:31','2018-07-13 07:49:59','postgres','DBEngineVersion=9.6.1',0,'DBEngine');
/*!40000 ALTER TABLE `infrastructure_parameter` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-11-29 13:14:20
