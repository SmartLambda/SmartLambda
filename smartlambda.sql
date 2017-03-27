
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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GitHubCredential` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `accessToken` varchar(255) NOT NULL,
  `user` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_GitHubCredential_accessToken` (`accessToken`),
  KEY `idx_GitHubCredential_user` (`user`),
  CONSTRAINT `ibfk_GitHubCredential_user` FOREIGN KEY (`user`) REFERENCES `User` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Key` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `user` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_Key_user_name` (`user`,`name`),
  KEY `idx_Key_user` (`user`),
  CONSTRAINT `ibfk_Key_user` FOREIGN KEY (`user`) REFERENCES `User` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Lambda` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `owner` int(11) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `runtime` varchar(255) NOT NULL,
  `async` tinyint(1) NOT NULL DEFAULT '0',
  `containerId` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_Lambda_owner_name` (`owner`,`name`),
  KEY `idx_Lambda_owner` (`owner`),
  CONSTRAINT `ibfk_Lambda_owner` FOREIGN KEY (`owner`) REFERENCES `User` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MonitoringEvent` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `time` datetime NOT NULL,
  `lambdaName` varchar(255) NOT NULL,
  `duration` bigint(20) unsigned DEFAULT NULL,
  `CPUTime` int(11) unsigned DEFAULT NULL,
  `error` mediumtext,
  `type` enum('EXECUTION','DELETION','DEPLOYMENT') NOT NULL,
  `lambdaOwner` int(11) unsigned NOT NULL,
  `key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_monitoringEvent_lambdaName_lambdaOwner` (`lambdaName`,`lambdaOwner`),
  KEY `idx_MonitoringEvent_lambdaOwner` (`lambdaOwner`),
  KEY `idx_MonitoringEvent_key` (`key`),
  CONSTRAINT `ibfk_MonitoringEvent_key` FOREIGN KEY (`key`) REFERENCES `Key` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `ibfk_MonitoringEvent_lambdaOwner` FOREIGN KEY (`lambdaOwner`) REFERENCES `User` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Permission` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `permissionType` enum('READ','PATCH','EXECUTE','DELETE','STATUS','SCHEDULE','CREATE','GRANT') NOT NULL,
  `key` varchar(255) NOT NULL,
  `user` int(11) unsigned DEFAULT NULL,
  `lambda` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_Permission_permissionType_key_user_lambda` (`permissionType`,`key`,`user`,`lambda`),
  KEY `idx_Permission_key` (`key`),
  KEY `idx_Permission_lambda` (`lambda`),
  KEY `idx_Permission_user` (`user`),
  CONSTRAINT `ibfk_Permission_key` FOREIGN KEY (`key`) REFERENCES `Key` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ibfk_Permission_lambda` FOREIGN KEY (`lambda`) REFERENCES `Lambda` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ibfk_Permission_user` FOREIGN KEY (`user`) REFERENCES `User` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ScheduleEvent` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `key` varchar(255) NOT NULL,
  `nextExecution` datetime NOT NULL,
  `cronExpression` varchar(255) NOT NULL,
  `parameters` longtext,
  `lock` datetime DEFAULT NULL,
  `lambda` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ScheduleEvent_lambda_name` (`lambda`,`name`),
  KEY `idx_ScheduleEvent_lambda` (`lambda`),
  KEY `idx_ScheduleEvent_key` (`key`),
  CONSTRAINT `ibfk_ScheduleEvent_key` FOREIGN KEY (`key`) REFERENCES `Key` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ibfk_ScheduleEvent_lambda` FOREIGN KEY (`lambda`) REFERENCES `Lambda` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `primaryKey` varchar(255) DEFAULT NULL,
  `isAdmin` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_User_name` (`name`),
  UNIQUE KEY `idx_User_primaryKey` (`primaryKey`),
  CONSTRAINT `ibfk_User_primaryKey` FOREIGN KEY (`primaryKey`) REFERENCES `Key` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

