-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: student_management
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `administrateur`
--

DROP TABLE IF EXISTS `administrateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `administrateur` (
  `id_admin` int(11) NOT NULL,
  `niveau_acces` enum('normal','super') DEFAULT 'normal',
  PRIMARY KEY (`id_admin`),
  CONSTRAINT `administrateur_ibfk_1` FOREIGN KEY (`id_admin`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `administrateur`
--

LOCK TABLES `administrateur` WRITE;
/*!40000 ALTER TABLE `administrateur` DISABLE KEYS */;
INSERT INTO `administrateur` VALUES (6,'super');
/*!40000 ALTER TABLE `administrateur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `anneescolaire`
--

DROP TABLE IF EXISTS `anneescolaire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `anneescolaire` (
  `id_annee` int(11) NOT NULL AUTO_INCREMENT,
  `libelle` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`id_annee`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `anneescolaire`
--

LOCK TABLES `anneescolaire` WRITE;
/*!40000 ALTER TABLE `anneescolaire` DISABLE KEYS */;
INSERT INTO `anneescolaire` VALUES (1,'2024/2025'),(2,'2025/2026');
/*!40000 ALTER TABLE `anneescolaire` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chefprogramme`
--

DROP TABLE IF EXISTS `chefprogramme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chefprogramme` (
  `id_chefprog` int(11) NOT NULL,
  `departement` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_chefprog`),
  CONSTRAINT `chefprogramme_ibfk_1` FOREIGN KEY (`id_chefprog`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chefprogramme`
--

LOCK TABLES `chefprogramme` WRITE;
/*!40000 ALTER TABLE `chefprogramme` DISABLE KEYS */;
INSERT INTO `chefprogramme` VALUES (4,'Informatique');
/*!40000 ALTER TABLE `chefprogramme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enseignant`
--

DROP TABLE IF EXISTS `enseignant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enseignant` (
  `id_enseignant` int(11) NOT NULL,
  `grade` enum('enseignant','chef_programme') DEFAULT 'enseignant',
  PRIMARY KEY (`id_enseignant`),
  CONSTRAINT `enseignant_ibfk_1` FOREIGN KEY (`id_enseignant`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enseignant`
--

LOCK TABLES `enseignant` WRITE;
/*!40000 ALTER TABLE `enseignant` DISABLE KEYS */;
INSERT INTO `enseignant` VALUES (3,'enseignant');
/*!40000 ALTER TABLE `enseignant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enseignant_matiere`
--

DROP TABLE IF EXISTS `enseignant_matiere`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enseignant_matiere` (
  `id_enseignant` int(11) NOT NULL,
  `id_matiere` int(11) NOT NULL,
  PRIMARY KEY (`id_enseignant`,`id_matiere`),
  KEY `id_matiere` (`id_matiere`),
  CONSTRAINT `enseignant_matiere_ibfk_1` FOREIGN KEY (`id_enseignant`) REFERENCES `enseignant` (`id_enseignant`),
  CONSTRAINT `enseignant_matiere_ibfk_2` FOREIGN KEY (`id_matiere`) REFERENCES `matiere` (`id_matiere`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enseignant_matiere`
--

LOCK TABLES `enseignant_matiere` WRITE;
/*!40000 ALTER TABLE `enseignant_matiere` DISABLE KEYS */;
INSERT INTO `enseignant_matiere` VALUES (3,1),(3,2),(3,3),(3,8);
/*!40000 ALTER TABLE `enseignant_matiere` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `epreuve`
--

DROP TABLE IF EXISTS `epreuve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `epreuve` (
  `id_epreuve` int(11) NOT NULL AUTO_INCREMENT,
  `type_epreuve` enum('controle','examen','projet','tp') DEFAULT NULL,
  `date_epreuve` date DEFAULT NULL,
  `id_matiere` int(11) DEFAULT NULL,
  `coefficient` double DEFAULT 0,
  PRIMARY KEY (`id_epreuve`),
  KEY `id_matiere` (`id_matiere`),
  CONSTRAINT `epreuve_ibfk_1` FOREIGN KEY (`id_matiere`) REFERENCES `matiere` (`id_matiere`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `epreuve`
--

LOCK TABLES `epreuve` WRITE;
/*!40000 ALTER TABLE `epreuve` DISABLE KEYS */;
INSERT INTO `epreuve` VALUES (1,'examen','2025-06-10',1,0.6),(2,'tp','2025-05-20',1,0.2),(3,'controle','2025-04-12',2,0.2),(17,'controle','2025-11-11',8,0.2);
/*!40000 ALTER TABLE `epreuve` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `etudiant`
--

DROP TABLE IF EXISTS `etudiant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `etudiant` (
  `id_etudiant` int(11) NOT NULL,
  `origine_scolaire` varchar(50) DEFAULT NULL,
  `statut` enum('admis','redoublant','exclu') DEFAULT 'admis',
  PRIMARY KEY (`id_etudiant`),
  CONSTRAINT `etudiant_ibfk_1` FOREIGN KEY (`id_etudiant`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `etudiant`
--

LOCK TABLES `etudiant` WRITE;
/*!40000 ALTER TABLE `etudiant` DISABLE KEYS */;
INSERT INTO `etudiant` VALUES (1,'DUT Informatique','admis'),(2,'CPI','admis'),(24,'CPI','redoublant'),(28,'DUT','admis');
/*!40000 ALTER TABLE `etudiant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inscription`
--

DROP TABLE IF EXISTS `inscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inscription` (
  `id_inscription` int(11) NOT NULL AUTO_INCREMENT,
  `id_etudiant` int(11) DEFAULT NULL,
  `id_programme` int(11) DEFAULT NULL,
  `id_annee` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_inscription`),
  KEY `id_etudiant` (`id_etudiant`),
  KEY `id_programme` (`id_programme`),
  KEY `id_annee` (`id_annee`),
  CONSTRAINT `inscription_ibfk_1` FOREIGN KEY (`id_etudiant`) REFERENCES `etudiant` (`id_etudiant`),
  CONSTRAINT `inscription_ibfk_2` FOREIGN KEY (`id_programme`) REFERENCES `programme` (`id_programme`),
  CONSTRAINT `inscription_ibfk_3` FOREIGN KEY (`id_annee`) REFERENCES `anneescolaire` (`id_annee`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inscription`
--

LOCK TABLES `inscription` WRITE;
/*!40000 ALTER TABLE `inscription` DISABLE KEYS */;
INSERT INTO `inscription` VALUES (1,1,1,1),(2,2,2,2),(7,24,2,1),(9,28,3,2);
/*!40000 ALTER TABLE `inscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `matiere`
--

DROP TABLE IF EXISTS `matiere`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `matiere` (
  `id_matiere` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) DEFAULT NULL,
  `coefficient` decimal(4,2) DEFAULT NULL,
  `id_programme` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_matiere`),
  KEY `id_programme` (`id_programme`),
  CONSTRAINT `matiere_ibfk_1` FOREIGN KEY (`id_programme`) REFERENCES `programme` (`id_programme`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `matiere`
--

LOCK TABLES `matiere` WRITE;
/*!40000 ALTER TABLE `matiere` DISABLE KEYS */;
INSERT INTO `matiere` VALUES (1,'Programmation Java',3.00,1),(2,'Bases de Données',2.00,1),(3,'Réseaux Informatiques',2.00,3),(4,'Algèbre Linéaire et Analyse',3.00,4),(8,'Génie Logiciel Distribué',3.00,2);
/*!40000 ALTER TABLE `matiere` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `note` (
  `id_note` int(11) NOT NULL AUTO_INCREMENT,
  `id_etudiant` int(11) DEFAULT NULL,
  `id_epreuve` int(11) DEFAULT NULL,
  `note` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id_note`),
  KEY `id_etudiant` (`id_etudiant`),
  KEY `id_epreuve` (`id_epreuve`),
  CONSTRAINT `note_ibfk_1` FOREIGN KEY (`id_etudiant`) REFERENCES `etudiant` (`id_etudiant`),
  CONSTRAINT `note_ibfk_2` FOREIGN KEY (`id_epreuve`) REFERENCES `epreuve` (`id_epreuve`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `note`
--

LOCK TABLES `note` WRITE;
/*!40000 ALTER TABLE `note` DISABLE KEYS */;
INSERT INTO `note` VALUES (1,1,1,14.50),(2,1,2,16.00),(3,1,3,12.00),(4,2,1,11.00),(5,2,2,13.50),(6,2,3,10.00),(12,24,1,9.50),(13,24,2,3.00),(14,24,3,10.00),(17,24,17,10.00),(18,2,17,12.00);
/*!40000 ALTER TABLE `note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notefinale`
--

DROP TABLE IF EXISTS `notefinale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notefinale` (
  `id_finale` int(11) NOT NULL AUTO_INCREMENT,
  `id_etudiant` int(11) NOT NULL,
  `id_matiere` int(11) NOT NULL,
  `moyenne_finale` decimal(5,2) NOT NULL,
  `valide` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id_finale`),
  UNIQUE KEY `id_etudiant` (`id_etudiant`,`id_matiere`),
  KEY `id_matiere` (`id_matiere`),
  CONSTRAINT `notefinale_ibfk_1` FOREIGN KEY (`id_etudiant`) REFERENCES `etudiant` (`id_etudiant`),
  CONSTRAINT `notefinale_ibfk_2` FOREIGN KEY (`id_matiere`) REFERENCES `matiere` (`id_matiere`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notefinale`
--

LOCK TABLES `notefinale` WRITE;
/*!40000 ALTER TABLE `notefinale` DISABLE KEYS */;
INSERT INTO `notefinale` VALUES (1,1,1,14.87,1),(2,2,1,11.63,1),(3,1,2,12.00,1),(4,2,2,10.00,1),(7,24,1,7.88,1),(10,24,2,10.00,1),(38,24,8,10.00,1),(39,2,8,12.00,1);
/*!40000 ALTER TABLE `notefinale` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prerequisprogramme`
--

DROP TABLE IF EXISTS `prerequisprogramme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prerequisprogramme` (
  `id_programme` int(11) NOT NULL,
  `id_programme_prerequis` int(11) NOT NULL,
  PRIMARY KEY (`id_programme`,`id_programme_prerequis`),
  KEY `id_programme_prerequis` (`id_programme_prerequis`),
  CONSTRAINT `prerequisprogramme_ibfk_1` FOREIGN KEY (`id_programme`) REFERENCES `programme` (`id_programme`),
  CONSTRAINT `prerequisprogramme_ibfk_2` FOREIGN KEY (`id_programme_prerequis`) REFERENCES `programme` (`id_programme`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prerequisprogramme`
--

LOCK TABLES `prerequisprogramme` WRITE;
/*!40000 ALTER TABLE `prerequisprogramme` DISABLE KEYS */;
INSERT INTO `prerequisprogramme` VALUES (2,1),(5,4);
/*!40000 ALTER TABLE `prerequisprogramme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `programme`
--

DROP TABLE IF EXISTS `programme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `programme` (
  `id_programme` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id_programme`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `programme`
--

LOCK TABLES `programme` WRITE;
/*!40000 ALTER TABLE `programme` DISABLE KEYS */;
INSERT INTO `programme` VALUES (1,'ISIL','Informatique des Systèmes d’Information et Logiciels'),(2,'GL','Génie Logiciel'),(3,'ISIN','Ingénierie des Systèmes d’Information et des Réseaux'),(4,'ING1 TC','ING Tronc Commun'),(5,'ING2 TC','ING2 Tronc Commun');
/*!40000 ALTER TABLE `programme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `programme_matiere`
--

DROP TABLE IF EXISTS `programme_matiere`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `programme_matiere` (
  `id_programme` int(11) NOT NULL,
  `id_matiere` int(11) NOT NULL,
  PRIMARY KEY (`id_programme`,`id_matiere`),
  KEY `id_matiere` (`id_matiere`),
  CONSTRAINT `programme_matiere_ibfk_1` FOREIGN KEY (`id_programme`) REFERENCES `programme` (`id_programme`) ON DELETE CASCADE,
  CONSTRAINT `programme_matiere_ibfk_2` FOREIGN KEY (`id_matiere`) REFERENCES `matiere` (`id_matiere`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `programme_matiere`
--

LOCK TABLES `programme_matiere` WRITE;
/*!40000 ALTER TABLE `programme_matiere` DISABLE KEYS */;
INSERT INTO `programme_matiere` VALUES (1,1),(1,2),(2,1),(2,2),(2,8),(3,3),(4,4);
/*!40000 ALTER TABLE `programme_matiere` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secretaire`
--

DROP TABLE IF EXISTS `secretaire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `secretaire` (
  `id_secretaire` int(11) NOT NULL,
  `bureau` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_secretaire`),
  CONSTRAINT `secretaire_ibfk_1` FOREIGN KEY (`id_secretaire`) REFERENCES `utilisateur` (`id_utilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secretaire`
--

LOCK TABLES `secretaire` WRITE;
/*!40000 ALTER TABLE `secretaire` DISABLE KEYS */;
INSERT INTO `secretaire` VALUES (5,'Bloc A - Bureau 12');
/*!40000 ALTER TABLE `secretaire` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `utilisateur` (
  `id_utilisateur` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `role` enum('etudiant','enseignant','chef_programme','secretaire','administrateur') NOT NULL,
  PRIMARY KEY (`id_utilisateur`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateur`
--

LOCK TABLES `utilisateur` WRITE;
/*!40000 ALTER TABLE `utilisateur` DISABLE KEYS */;
INSERT INTO `utilisateur` VALUES (1,'Benali','Amina','amina.benali@univ.tn','pass123','etudiant'),(2,'Khaled','Rami','rami.khaled@univ.tn','pass123','etudiant'),(3,'Dridi','Salim','salim.dridi@univ.tn','pass123','enseignant'),(4,'Bouzid','Nour','nour.bouzid@univ.tn','pass123','chef_programme'),(5,'Talabi','Mouna','mouna.talabi@univ.tn','pass123','secretaire'),(6,'Admin','Admin','admin','admin','administrateur'),(24,'Ait','Mouna','ait.mouna@univ.tn','pass123','etudiant'),(28,'Chlgmi','Mohamed','mohamed.chlgmi@univ.tn','pass123','etudiant');
/*!40000 ALTER TABLE `utilisateur` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-11 22:13:43
