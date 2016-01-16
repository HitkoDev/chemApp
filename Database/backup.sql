-- MySQL dump 10.13  Distrib 5.5.46, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: amaranth
-- ------------------------------------------------------
-- Server version	5.5.46-0+deb8u1

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
-- Table structure for table `modx_chemapp_level`
--

DROP TABLE IF EXISTS `modx_chemapp_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modx_chemapp_level` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `description` mediumtext NOT NULL,
  `order` int(10) NOT NULL DEFAULT '999999',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modx_chemapp_level`
--

LOCK TABLES `modx_chemapp_level` WRITE;
/*!40000 ALTER TABLE `modx_chemapp_level` DISABLE KEYS */;
INSERT INTO `modx_chemapp_level` VALUES (1,'Osnova šola','Snov, ki spada v osnovno šolo',0),(2,'Gimnazija','Snov, ki spada v gimnazijo',1);
/*!40000 ALTER TABLE `modx_chemapp_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modx_chemapp_multiple_choices`
--

DROP TABLE IF EXISTS `modx_chemapp_multiple_choices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modx_chemapp_multiple_choices` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `question` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modx_chemapp_multiple_choices`
--

LOCK TABLES `modx_chemapp_multiple_choices` WRITE;
/*!40000 ALTER TABLE `modx_chemapp_multiple_choices` DISABLE KEYS */;
/*!40000 ALTER TABLE `modx_chemapp_multiple_choices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modx_chemapp_multiple_choices_answer`
--

DROP TABLE IF EXISTS `modx_chemapp_multiple_choices_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modx_chemapp_multiple_choices_answer` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `answer` mediumtext,
  `explanation` mediumtext,
  `question` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modx_chemapp_multiple_choices_answer`
--

LOCK TABLES `modx_chemapp_multiple_choices_answer` WRITE;
/*!40000 ALTER TABLE `modx_chemapp_multiple_choices_answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `modx_chemapp_multiple_choices_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modx_chemapp_section`
--

DROP TABLE IF EXISTS `modx_chemapp_section`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modx_chemapp_section` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `description` mediumtext NOT NULL,
  `icon` varchar(100) DEFAULT NULL,
  `level` int(10) NOT NULL DEFAULT '0',
  `parent` int(10) NOT NULL DEFAULT '0',
  `order` int(10) NOT NULL DEFAULT '999999',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modx_chemapp_section`
--

LOCK TABLES `modx_chemapp_section` WRITE;
/*!40000 ALTER TABLE `modx_chemapp_section` DISABLE KEYS */;
INSERT INTO `modx_chemapp_section` VALUES (1,'Atom in periodni sistem elementov','Zgradba atoma, atomsko jedro, elektronska ovojnica, vsrtno in masno število, periodni sistem lelemtov - skupina, ioni.',NULL,1,0,0),(2,'Družina ogljikovodikov','Vir ogljikovodikov v naravi, elementa ogljik in vodik, zgradba, delitev in poimenovanje ogljikovodikov, reaktivnost, lastnosti glede na zgradbo.','',1,0,1),(3,'Dušikova družina organskih spojin','Lastnosti dušikovih organskih snovi, aminoskupina, aminokisline, beljakovine, lastnosti beljakovin, pomen beljakovin v oranizmih, sintezni poliamidni polimeri.',NULL,1,0,2),(4,'Elementi v periodnem sistemu','Naravni viri elementov, relativna atomska in molekulska masa, masni delež, lega elementov v periodnem sistemu, nekatere skupine periodnega sistema : alkalijske kovine, zemljoalkalijske kovine, železo, baker, živo srebro, zlato, halogeni, žlahtni plini.',NULL,1,0,3),(5,'Kemija - svet snovi','Kaj je kemija, kemijski poklici, snovi, agregatna stanja, atomi, molekule - kaj so.',NULL,1,0,4),(6,'Kemijske reakcije','Kemijska sprememba, zakon o ohranitvi mase, reaktanti in produkti, urejanje kemijskih enačb, eksotermne in endoterme reakcije.',NULL,1,0,5),(7,'Kisikova družina organskih spojin','Skupine organskih kisikovih spojin (alkoholi, estri, karboskilne kisline), lastnosti in uporaba organskih kisikovih spojin, maščobe, mila, ogljikohidrati in njihove lastnosti, kondenzacijski polimeri.',NULL,1,0,6),(8,'Kisline, baze in soli','Opredelitev kislin in baz, indikatorji, nevtralizacija, pH-lestvica, raztopine, topnost, soli, masni delež v raztopini',NULL,1,0,7),(9,'Množina snovi','Množina snovi, mol, molska masa snovi.','OŠ/Množina snovi/ozon.png',1,0,8),(10,'Povezovanje delcev','Vsebuje:\n	- Ionska vez,\n	- Kovalentna vez,\n	- Lastnosti kovalentnih in ionskih vezi.',NULL,1,0,9),(11,'Alkalijske snovi in halogeni','Kemijske in fizikalne lastnosti alkalijskih kovin in halogenov ter njihovih spojin, reakcije alkalijskih kovin z vodo, kisikom in halogeni, vodikovi halogenidi.',NULL,2,0,0),(12,'Atom in periodni sistem elementov','Zgradba atoma, atomsko jedro, elektronska ovojnica, vsrtno in masno število, periodni sistem lelemtov - skupina, ioni.',NULL,2,0,999999),(13,'Delci snovi','Zgradba atoma: osnovni delci v atomu, relatisna atomska masa, izotopi, zgradba elektronske ovojnice, atomski radij, ioni.',NULL,2,0,999999),(14,'Elementi v periodnem sistemu','',NULL,2,0,999999),(15,'Področja v periodnem sistemu','Področja elementov v periodnem sistemu: s, p, d in f, značilnosti elementov v posameznem področju, oksidi tretje periode.','',2,14,999999),(16,'Prehodni elementi in koordinacijske spojine','Značilnosti prehodnih elementov, glavni tipi spojin, uporaba prehodnih elementov v industriji, krom, železo, koordinacijske spojine. ',NULL,2,14,999999),(17,'Kisline, baze in soli','',NULL,2,0,999999),(18,'Potek kemijskih reakcij','',NULL,2,0,999999),(19,'Hitrost kemijskih reakcij','Vplivi na hitrost kemijske reakcije (koncentracija, temperatura, površina delcev), kataliza, katalizator, katalizatorji v avtomobilih, encimi, hitrost reakcije na ravni delcev, teorija trka, aktivacijska energija.',NULL,2,18,999999),(20,'Kemijsko ravnotežje','Zakon o vplivu koncentracij, vplivi na položaj ravnotežja kemijske reakcije – le chatelierovo načelo, industrijska sinteza amoniaka.',NULL,2,18,999999),(35,'Simbolni zapisi','','',2,25,999999),(23,'Povezovanje delcev','Ionska vez, ionske spojine, kovalentna vez, kovalentne spojine, jakost vezi, elektronegativnost, vezni in nevezni elektronski pari, oblika enostavnih molekul, kovinska vez in fizikalne lastnosti kovin, poimenovanje binarnih spojin po nomenklaturi IUPAC, molekulske vezi, vodikova vez, molekulski kristali.',NULL,2,0,999999),(24,'Raztopine','Sestava raztopin, masni delež, množinska in masna koncentracija raztopin, vplivi na topnost snovi, proces hidratacije.',NULL,2,0,999999),(25,'Simbolni zapisi in množina snovi','2 podpoglavlja: množina snovi, enačba kemijske reakcije kot simbolni zapis.',NULL,2,0,999999),(26,'Množina snovi','Molska masa, množina snovi, molska prostornina plinov.',NULL,2,25,999999),(29,'Zgradba in lastnosti ogljikovodikov','Fizikalne lastnosti ogljikovodikov, izomerija ogljikovodikov in vplivi na izbrane fizikalne lastnosti ogljikovodikov, osnove organskih reakcij: substrat, reagent, homolitska in heterolitska prekinitev vezi, radikal, elektrofil, nukleofil, reaktivnost ogljikovodikov: alkani, radikalska substitucija, alkeni, elektrofilna adicija, adicijska polimerizacija, aromati, elektrofilna aromatska substitucija na benzenu, oksidativna razgradnja ogljikovodikov, uporaba in vplivi na okolje.',NULL,2,0,999999),(30,'Zgradba in lastnosti organskih dušikovih spojin','',NULL,2,0,999999),(31,'Amini kot baze','Bazičnost aminov, delitev organskih aminov, osnove poimenovanja, alkaloidi, reakcije aminov s kislinami in nastajanje soli.',NULL,2,30,999999),(32,'Aminokisline','Kiralni center, L-aminokisline, amfoternost aminokislin, bipolarni značaj aminokislin, dokazne reakcije aminokislin (ninhidrinski test, biuretska reakcija), beljakovine, pomen v prehrani.',NULL,2,30,999999);
/*!40000 ALTER TABLE `modx_chemapp_section` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-01-16 14:13:00
