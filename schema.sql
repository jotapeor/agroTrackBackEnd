CREATE DATABASE  IF NOT EXISTS `agrotrack_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `agrotrack_db`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: agrotrack_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abastecimento`
--

DROP TABLE IF EXISTS `abastecimento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abastecimento` (
  `id_abastecimento` int NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `id_usuario` int NOT NULL,
  `data_abastecimento` datetime DEFAULT CURRENT_TIMESTAMP,
  `litros` decimal(8,2) NOT NULL,
  `tipo_combustivel` varchar(50) NOT NULL,
  `hodometro_atual` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_abastecimento`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `abastecimento_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `abastecimento_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abastecimento`
--

LOCK TABLES `abastecimento` WRITE;
/*!40000 ALTER TABLE `abastecimento` DISABLE KEYS */;
INSERT INTO `abastecimento` VALUES (1,1,2,'2026-06-20 06:45:00',150.00,'Diesel S10',1240.50),(2,2,3,'2026-06-21 05:30:00',400.00,'Diesel S500',420.00),(3,3,2,'2026-06-22 07:45:00',200.00,'Diesel S10',3200.80),(4,4,5,'2026-06-24 06:20:00',180.00,'Diesel S10',890.30),(5,5,4,'2026-06-24 07:00:00',320.00,'Diesel S500',120.00),(6,6,6,'2026-06-24 07:40:00',210.00,'Diesel S10',2100.60);
/*!40000 ALTER TABLE `abastecimento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `autorizacao_risco`
--

DROP TABLE IF EXISTS `autorizacao_risco`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `autorizacao_risco` (
  `id_autorizacao` int NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `id_proprietario` int NOT NULL,
  `justificativa` text NOT NULL,
  `data_autorizacao` datetime NOT NULL,
  PRIMARY KEY (`id_autorizacao`),
  KEY `fk_autorizacao_maquina` (`id_maquina`),
  KEY `fk_autorizacao_proprietario` (`id_proprietario`),
  CONSTRAINT `fk_autorizacao_maquina` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE CASCADE,
  CONSTRAINT `fk_autorizacao_proprietario` FOREIGN KEY (`id_proprietario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `autorizacao_risco`
--

LOCK TABLES `autorizacao_risco` WRITE;
/*!40000 ALTER TABLE `autorizacao_risco` DISABLE KEYS */;
INSERT INTO `autorizacao_risco` VALUES (1,4,1,'Safra necessaria','2026-08-06 14:58:29');
/*!40000 ALTER TABLE `autorizacao_risco` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fazenda`
--

DROP TABLE IF EXISTS `fazenda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fazenda` (
  `id_fazenda` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `localizacao` varchar(255) DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  `data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_fazenda`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fazenda`
--

LOCK TABLES `fazenda` WRITE;
/*!40000 ALTER TABLE `fazenda` DISABLE KEYS */;
INSERT INTO `fazenda` VALUES (1,'Fazenda Boa Esperança','Rodovia BR-364, Km 120, Zona Rural',1,'2026-07-01 08:00:00'),(2,'Fazenda Santa Fé','Estrada Municipal 456, Lote 12',1,'2026-07-01 08:00:00'),(3,'Fazenda Nossa Senhora Aparecida','Rodovia SP-225, Km 45, Zona Rural',1,'2026-07-01 08:05:00');
/*!40000 ALTER TABLE `fazenda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historico_status_maquina`
--

DROP TABLE IF EXISTS `historico_status_maquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historico_status_maquina` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `id_usuario` int NOT NULL,
  `status_anterior` varchar(50) NOT NULL,
  `novo_status` varchar(50) NOT NULL,
  `motivo` text NOT NULL,
  `data_alteracao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_hsm_maquina` (`id_maquina`),
  KEY `fk_hsm_usuario` (`id_usuario`),
  CONSTRAINT `fk_hsm_maquina` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `fk_hsm_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historico_status_maquina`
--

LOCK TABLES `historico_status_maquina` WRITE;
/*!40000 ALTER TABLE `historico_status_maquina` DISABLE KEYS */;
/*!40000 ALTER TABLE `historico_status_maquina` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `maquina`
--

DROP TABLE IF EXISTS `maquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maquina` (
  `id_maquina` int NOT NULL AUTO_INCREMENT,
  `id_fazenda` int DEFAULT NULL,
  `id_talhao` int DEFAULT NULL,
  `nome` varchar(100) NOT NULL,
  `tipo` enum('Trator','Colheitadeira','Pulverizador','Semeadeira') NOT NULL,
  `marca` varchar(100) DEFAULT NULL,
  `modelo` varchar(100) NOT NULL,
  `ano` int NOT NULL,
  `numero_serie` varchar(100) DEFAULT NULL,
  `placa` varchar(20) DEFAULT NULL,
  `hodometro_inicial` decimal(10,2) NOT NULL DEFAULT '0.00',
  `capacidade_tanque` decimal(10,2) DEFAULT NULL,
  `tipo_combustivel` varchar(50) DEFAULT NULL,
  `intervalo_troca_oleo_horas` int DEFAULT NULL,
  `intervalo_inspecao_horas` int DEFAULT NULL,
  `consumo_medio` decimal(5,2) DEFAULT NULL,
  `status` enum('Disponivel','Em Operacao','Em Manutencao','Inativa') DEFAULT 'Disponivel',
  `nivel_risco` enum('Baixo','Medio','Alto') DEFAULT 'Baixo',
  `data_aquisicao` date DEFAULT NULL,
  `valor_aquisicao` decimal(12,2) DEFAULT NULL,
  `foto_path` varchar(255) DEFAULT NULL,
  `observacoes` text,
  `data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `autorizada_operacao_risco` tinyint(1) DEFAULT '0',
  `ativo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_maquina`),
  KEY `id_fazenda` (`id_fazenda`),
  KEY `id_talhao` (`id_talhao`),
  CONSTRAINT `maquina_ibfk_1` FOREIGN KEY (`id_fazenda`) REFERENCES `fazenda` (`id_fazenda`) ON DELETE SET NULL,
  CONSTRAINT `maquina_ibfk_2` FOREIGN KEY (`id_talhao`) REFERENCES `talhao` (`id_talhao`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `maquina`
--

LOCK TABLES `maquina` WRITE;
/*!40000 ALTER TABLE `maquina` DISABLE KEYS */;
INSERT INTO `maquina` VALUES (1,1,1,'Trator 01 - John Deere','Trator','John Deere','6100J',2021,'9YZ1234XG1234567','ABC-1234',1280.00,250.00,'Diesel S10',250,500,12.50,'Disponivel','Baixo','2020-01-15',320000.00,'cb34ee88-a16f-4947-b2c9-988a536b516b_John Deere 6100J.jpeg','Trator principal para preparo de solo.','2026-06-23 00:00:00',0,1),(2,1,2,'Colheitadeira Case IH','Colheitadeira','Case IH','250 Series',2023,'H8T5678AB9012345','DEF-5678',450.00,400.00,'Diesel S500',200,300,35.00,'Disponivel','Baixo','2023-03-10',890000.00,'779d2ce9-1715-4917-9b59-a953f264166f_Case IH 250 Series.jpg','Colheitadeira de alta capacidade para grãos.','2026-06-23 00:00:00',0,1),(3,2,4,'Pulverizador Jacto 3030','Pulverizador','Jacto','Uniport 3030',2020,'P3X9012CD3456789',NULL,3200.80,300.00,'Diesel S10',250,400,18.20,'Disponivel','Baixo','2020-06-20',450000.00,'cc0e7a35-3c66-42aa-aadd-04d43e68cbc6_Jacto Uniport 3030.jpg','Apresentou falha na bomba hidráulica.','2026-06-23 00:00:00',0,1),(4,2,5,'Trator MF 4275','Trator','Massey Ferguson','4275',2022,'M4F3456EF7890123','GHI-9012',920.00,250.00,'Diesel S10',250,500,13.80,'Disponivel','Alto','2022-02-10',280000.00,'006705f9-a314-4d81-91f1-d714540a3061_Massey Ferguson 4275.jpg','Utilizado em operações de plantio direto.','2026-06-24 00:00:00',0,1),(5,3,6,'Colheitadeira NH CR6.90','Colheitadeira','New Holland','CR6.90',2024,'N5H7890GH1234567','JKL-3456',130.00,450.00,'Diesel S500',200,300,40.50,'Disponivel','Baixo','2024-01-05',950000.00,'7dfed867-6c2a-45b3-bb25-a044a84752a5_New Holland CR6.90.jpg','Colheitadeira nova para a safra de café.','2026-06-24 00:00:00',0,1),(6,3,7,'Pulverizador Stara','Pulverizador','Stara','Imperador 3.0',2021,'S6T1234IJ5678901',NULL,2100.60,350.00,'Diesel S10',250,400,17.90,'Disponivel','Baixo','2021-05-15',520000.00,'25ba326b-d088-47de-a64f-17fbe1e1d317_Stara Imperador 3.0.jpg','Pulverizador para aplicação de defensivos.','2026-06-24 00:00:00',0,1);
/*!40000 ALTER TABLE `maquina` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `maquina_combustivel`
--

DROP TABLE IF EXISTS `maquina_combustivel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maquina_combustivel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `tipo_combustivel` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_maquina` (`id_maquina`),
  CONSTRAINT `maquina_combustivel_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `maquina_combustivel`
--

LOCK TABLES `maquina_combustivel` WRITE;
/*!40000 ALTER TABLE `maquina_combustivel` DISABLE KEYS */;
/*!40000 ALTER TABLE `maquina_combustivel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificacao`
--

DROP TABLE IF EXISTS `notificacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacao` (
  `id_notificacao` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `id_maquina` int DEFAULT NULL,
  `id_ordem_manutencao` int DEFAULT NULL,
  `tipo` varchar(50) NOT NULL,
  `mensagem` text NOT NULL,
  `lida` tinyint(1) DEFAULT '0',
  `data_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_notificacao`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_ordem_manutencao` (`id_ordem_manutencao`),
  CONSTRAINT `notificacao_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `notificacao_ibfk_2` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE SET NULL,
  CONSTRAINT `notificacao_ibfk_3` FOREIGN KEY (`id_ordem_manutencao`) REFERENCES `ordem_manutencao` (`id_ordem`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacao`
--

LOCK TABLES `notificacao` WRITE;
/*!40000 ALTER TABLE `notificacao` DISABLE KEYS */;
INSERT INTO `notificacao` VALUES (1,1,1,NULL,'alerta_preventivo','O Trator John Deere está se aproximando do limite de quilometragem para a próxima revisão (faltam 50km).',0,'2026-06-23 13:45:33'),(2,1,2,2,'ordem_pendente','Nova Ordem de Manutenção pendente de aprovação para a Colheitadeira Case IH.',0,'2026-06-23 13:45:33'),(3,2,3,3,'anomalia','Falha crítica registrada no Pulverizador Jacto. Máquina marcada como Inativa/Em Manutenção.',1,'2026-06-23 13:45:33'),(4,6,5,5,'ordem_pendente','Nova Ordem de Manutenção pendente de aprovação para a Colheitadeira New Holland.',0,'2026-06-24 11:05:00'),(5,4,4,NULL,'alerta_preventivo','O Trator Massey Ferguson está se aproximando do limite de quilometragem para a próxima revisão (faltam 30km).',0,'2026-06-24 11:10:00'),(6,5,6,6,'anomalia','Pulverizador Stara apresenta risco elevado devido à verificação pendente do sistema hidráulico.',0,'2026-06-24 11:15:00'),(7,1,1,NULL,'alerta_preventivo','Trator John Deere próximo da troca de óleo. Hodômetro: 1250.50',0,'2026-06-20 08:00:00'),(8,1,2,NULL,'anomalia','Anomalia de consumo detectada na Colheitadeira Case IH. Consumo atual: 42.00, Média: 35.00',0,'2026-06-20 10:30:00'),(9,1,1,NULL,'alerta_preventivo','Trator John Deere próximo da inspeção. Hodômetro: 1250.50',0,'2026-06-21 07:00:00'),(10,1,4,NULL,'alerta_preventivo','Trator MF 4275 próximo da troca de óleo. Hodômetro: 890.30',0,'2026-06-22 09:00:00'),(11,1,3,NULL,'anomalia','Anomalia de consumo detectada no Pulverizador Jacto. Consumo atual: 25.00, Média: 18.20',0,'2026-06-22 14:00:00'),(12,1,5,NULL,'anomalia','Anomalia de consumo detectada na Colheitadeira NH. Consumo atual: 50.00, Média: 40.50',0,'2026-06-23 11:00:00'),(13,1,6,NULL,'alerta_preventivo','Pulverizador Stara próximo da inspeção. Hodômetro: 2100.60',0,'2026-06-24 08:00:00'),(14,1,2,NULL,'alerta_preventivo','Colheitadeira Case IH próxima da troca de óleo. Hodômetro: 450.00',0,'2026-06-24 15:00:00'),(15,1,1,NULL,'anomalia','Anomalia de consumo detectada no Trator John Deere. Consumo atual: 18.00, Média: 12.50',0,'2026-07-01 09:00:00'),(16,1,3,NULL,'alerta_preventivo','Pulverizador Jacto próximo da troca de óleo. Hodômetro: 3200.80',0,'2026-07-05 10:00:00'),(17,1,4,NULL,'anomalia','Anomalia de consumo detectada no Trator MF 4275. Consumo atual: 20.00, Média: 13.80',0,'2026-07-10 11:00:00'),(18,1,5,NULL,'alerta_preventivo','Colheitadeira NH próxima da inspeção. Hodômetro: 120.00',0,'2026-07-15 08:30:00'),(19,4,NULL,5,'ordem_aprovada','A ordem de manutenção para a máquina Colheitadeira NH CR6.90 foi encerrada.',0,'2026-07-31 17:09:41'),(20,2,NULL,3,'ordem_aprovada','A ordem de manutenção para a máquina Pulverizador Jacto 3030 foi encerrada.',0,'2026-07-31 17:12:15'),(21,1,NULL,7,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:01:53'),(22,6,NULL,7,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:01:53'),(23,7,NULL,7,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:01:53'),(24,1,NULL,8,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:09'),(25,6,NULL,8,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:09'),(26,7,NULL,8,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:09'),(27,1,NULL,9,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:22'),(28,6,NULL,9,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:22'),(29,7,NULL,9,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:02:22'),(30,1,NULL,10,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:03:04'),(31,6,NULL,10,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:03:04'),(32,7,NULL,10,'ordem_pendente','Nova ordem de manutenção aberta por Carlos Mendes para a máquina Trator 01 - John Deere aguardando aprovação.',0,'2026-08-06 14:03:04'),(33,2,NULL,10,'ordem_aprovada','Sua ordem de manutenção para a máquina Trator 01 - John Deere foi aprovada.',0,'2026-08-06 14:08:40'),(34,2,NULL,10,'ordem_aprovada','A ordem de manutenção para a máquina Trator 01 - John Deere foi encerrada.',0,'2026-08-06 14:10:39'),(35,2,NULL,9,'ordem_recusada','Sua ordem de manutenção para a máquina Trator 01 - John Deere foi recusada.',0,'2026-08-06 14:45:38'),(36,2,NULL,8,'ordem_recusada','Sua ordem de manutenção para a máquina Trator 01 - John Deere foi recusada.',0,'2026-08-06 14:45:41'),(37,2,NULL,7,'ordem_recusada','Sua ordem de manutenção para a máquina Trator 01 - John Deere foi recusada.',0,'2026-08-06 14:45:45'),(38,5,NULL,6,'ordem_aprovada','Sua ordem de manutenção para a máquina Pulverizador Stara foi aprovada.',0,'2026-08-06 14:47:05'),(39,5,NULL,6,'ordem_aprovada','A ordem de manutenção para a máquina Pulverizador Stara foi encerrada.',0,'2026-08-06 14:47:17'),(40,1,NULL,2,'ordem_aprovada','Sua ordem de manutenção para a máquina Colheitadeira Case IH foi aprovada.',0,'2026-08-06 14:47:21');
/*!40000 ALTER TABLE `notificacao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordem_manutencao`
--

DROP TABLE IF EXISTS `ordem_manutencao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordem_manutencao` (
  `id_ordem` int NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `id_solicitante` int NOT NULL,
  `status` enum('Aguardando Aprovação','Ativa','Encerrada','Recusada') NOT NULL,
  `prioridade` enum('Baixa','Media','Alta','Critica') NOT NULL,
  `descricao` text NOT NULL,
  `observacao_encerramento` text,
  `data_abertura` datetime DEFAULT CURRENT_TIMESTAMP,
  `data_encerramento` datetime DEFAULT NULL,
  `removida_da_aba` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_ordem`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_solicitante` (`id_solicitante`),
  CONSTRAINT `ordem_manutencao_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `ordem_manutencao_ibfk_2` FOREIGN KEY (`id_solicitante`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordem_manutencao`
--

LOCK TABLES `ordem_manutencao` WRITE;
/*!40000 ALTER TABLE `ordem_manutencao` DISABLE KEYS */;
INSERT INTO `ordem_manutencao` VALUES (1,1,2,'Encerrada','Baixa','Troca de óleo do motor e filtros.','Serviço realizado pela oficina parceira.','2026-05-10 10:00:00','2026-05-11 15:00:00',1),(2,2,1,'Encerrada','Media','Revisão das correias e rolamentos antes da safra.','Peças trocadas','2026-06-23 09:00:00','2026-08-06 14:47:27',1),(3,3,2,'Encerrada','Alta','Substituição da bomba de pulverização estourada.','serviço realizado\r\n','2026-06-22 09:30:00','2026-07-31 17:12:15',1),(4,4,6,'Encerrada','Baixa','Troca de filtro de ar e verificação de pneus.','Manutenção concluída sem intercorrências.','2026-06-15 09:00:00','2026-06-15 14:00:00',1),(5,5,4,'Encerrada','Media','Ajuste do sistema de trilha e limpeza dos sensores.','serviço realizado','2026-06-24 10:00:00','2026-07-31 17:09:41',1),(6,6,5,'Encerrada','Alta','Verificação do sistema hidráulico do pulverizador.','Mangueira trocada','2026-06-24 11:00:00','2026-08-06 14:47:17',1),(7,1,2,'Recusada','Baixa','ruido anormal no sistema de trilha',NULL,'2026-08-06 14:01:53',NULL,1),(8,1,2,'Recusada','Media','ruido',NULL,'2026-08-06 14:02:09',NULL,1),(9,1,2,'Recusada','Media','ruido 2',NULL,'2026-08-06 14:02:22',NULL,1),(10,1,2,'Encerrada','Critica','111','Rolamento trocado\r\n','2026-08-06 14:03:04','2026-08-06 14:10:39',1),(11,4,1,'Encerrada','Baixa','Motor fervendo','Radiador consertado','2026-08-06 14:55:02','2026-08-06 14:56:02',0),(12,4,1,'Ativa','Critica','Pneu furado',NULL,'2026-08-06 14:56:20',NULL,0);
/*!40000 ALTER TABLE `ordem_manutencao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registro_operacao`
--

DROP TABLE IF EXISTS `registro_operacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registro_operacao` (
  `id_operacao` int NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `id_usuario` int NOT NULL,
  `data_inicio` datetime NOT NULL,
  `data_fim` datetime DEFAULT NULL,
  `hodometro_inicio` decimal(10,2) NOT NULL,
  `hodometro_fim` decimal(10,2) DEFAULT NULL,
  `peso_carregado` decimal(8,2) DEFAULT NULL,
  `observacoes` text,
  `peso_final` decimal(10,2) DEFAULT NULL COMMENT 'Peso colhido ao encerrar (apenas Colheitadeira)',
  PRIMARY KEY (`id_operacao`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `registro_operacao_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `registro_operacao_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registro_operacao`
--

LOCK TABLES `registro_operacao` WRITE;
/*!40000 ALTER TABLE `registro_operacao` DISABLE KEYS */;
INSERT INTO `registro_operacao` VALUES (1,1,2,'2026-06-20 07:00:00','2026-06-20 17:00:00',1240.50,1250.50,NULL,'Gradeação do talhão 4.',NULL),(2,2,3,'2026-06-21 06:00:00','2026-06-21 18:00:00',420.00,450.00,15000.00,'Colheita de soja no talhão 2.',12500.00),(3,3,2,'2026-06-22 08:00:00','2026-06-22 17:00:00',3200.80,3210.00,NULL,'Encerrado manualmente - correção de dados de seed',NULL),(4,4,5,'2026-06-24 07:00:00','2026-06-24 16:00:00',870.00,890.30,NULL,'Preparo de solo no talhão 6.',NULL),(5,5,4,'2026-06-24 06:30:00','2026-07-31 16:51:07',100.00,130.00,NULL,NULL,1300.00),(6,6,6,'2026-06-24 08:00:00','2026-06-24 12:00:00',2080.00,2100.60,NULL,'Aplicação de defensivo no talhão 5.',NULL),(7,4,1,'2026-07-31 17:24:34','2026-08-03 15:30:38',890.30,895.00,NULL,NULL,NULL),(8,1,2,'2026-08-05 17:23:10','2026-08-05 17:25:24',1250.50,1255.00,NULL,NULL,NULL),(9,1,2,'2026-08-05 17:27:37','2026-08-05 17:29:10',1255.00,1260.00,NULL,NULL,NULL),(10,1,2,'2026-08-05 17:29:15','2026-08-05 17:30:01',1260.00,1265.00,NULL,NULL,NULL),(11,4,2,'2026-08-06 13:46:10','2026-08-06 13:49:32',895.00,900.00,NULL,NULL,NULL),(12,1,2,'2026-08-06 13:58:14','2026-08-06 14:59:47',1265.00,1270.00,NULL,NULL,NULL),(13,4,2,'2026-08-06 14:59:52','2026-08-06 15:14:40',900.00,920.00,NULL,NULL,NULL),(14,1,2,'2026-08-06 15:14:47','2026-08-06 15:15:02',1270.00,1280.00,NULL,NULL,NULL);
/*!40000 ALTER TABLE `registro_operacao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `talhao`
--

DROP TABLE IF EXISTS `talhao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `talhao` (
  `id_talhao` int NOT NULL AUTO_INCREMENT,
  `id_fazenda` int NOT NULL,
  `nome` varchar(100) NOT NULL,
  `area_hectares` decimal(10,2) DEFAULT NULL,
  `cultura_atual` varchar(100) DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id_talhao`),
  KEY `id_fazenda` (`id_fazenda`),
  CONSTRAINT `talhao_ibfk_1` FOREIGN KEY (`id_fazenda`) REFERENCES `fazenda` (`id_fazenda`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `talhao`
--

LOCK TABLES `talhao` WRITE;
/*!40000 ALTER TABLE `talhao` DISABLE KEYS */;
INSERT INTO `talhao` VALUES (1,1,'Talhão 1 - Soja',45.00,'Soja',1),(2,1,'Talhão 2 - Milho',32.50,'Milho',1),(3,1,'Talhão 3 - Pastagem',28.00,'Pastagem',1),(4,2,'Talhão Norte',50.00,'Soja',1),(5,2,'Talhão Sul',38.00,'Milho',1),(6,3,'Talhão Leste',22.00,'Café',1),(7,3,'Talhão Oeste',30.00,'Cana-de-Açúcar',1);
/*!40000 ALTER TABLE `talhao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telemetria_maquina`
--

DROP TABLE IF EXISTS `telemetria_maquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telemetria_maquina` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_maquina` int NOT NULL,
  `velocidade_atual` decimal(10,2) DEFAULT NULL,
  `consumo_atual` decimal(10,2) DEFAULT NULL,
  `latitude` decimal(11,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `data_atualizacao` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_telemetria_maquina` (`id_maquina`),
  CONSTRAINT `fk_telemetria_maquina` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telemetria_maquina`
--

LOCK TABLES `telemetria_maquina` WRITE;
/*!40000 ALTER TABLE `telemetria_maquina` DISABLE KEYS */;
INSERT INTO `telemetria_maquina` VALUES (1,5,4.76,45.14,-12.95818634,-55.92563373,'2026-07-31 16:51:00'),(2,4,10.65,14.72,-12.95512223,-55.91639694,'2026-08-06 15:14:33'),(3,1,10.18,11.93,-12.98925741,-55.92571414,'2026-08-06 15:14:57');
/*!40000 ALTER TABLE `telemetria_maquina` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `perfil` enum('PROPRIETARIO','SOCIO','OPERADOR') NOT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  `primeiro_acesso` tinyint(1) DEFAULT '1',
  `foto_path` varchar(255) DEFAULT NULL,
  `data_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'João Batista','joao.batista@fazenda.com.br','$2a$10$rrPKOP/6TnNqBCJ9KAJetu/VwQvwOeKM8mkrLnydjq9q7COPJ3/Y2','PROPRIETARIO',1,0,'6f16d356-d842-4992-ab96-fc02fa7f3a4b_joao.png','2026-06-23 00:00:00'),(2,'Carlos Mendes','carlos.mendes@fazenda.com.br','$2a$10$rrPKOP/6TnNqBCJ9KAJetu/VwQvwOeKM8mkrLnydjq9q7COPJ3/Y2','OPERADOR',1,0,'8d1b85a8-a8e4-403f-824e-ca9df204f498_carlos.jpg','2026-06-23 00:00:00'),(3,'Marcos Silva','marcos.silva@fazenda.com.br','$2a$10$sERLal.GmDv1op88qKYAIenoycMQk5xcuipmwi48OJobAEnNeggX2','OPERADOR',1,0,'78bebcdd-3724-40ae-9078-c64363ba369c_marcos.jpg','2026-06-23 00:00:00'),(4,'Fernanda Rocha','fernanda.rocha@fazenda.com.br','$2a$10$XZPe87ZgL8eY28yEu0dEE.54/owZDBqnarBP/Mw1p5I5XzYyzBshO','OPERADOR',1,0,'07610657-f37c-4937-a743-23cfc96ecc28_fernanda.png','2026-06-24 00:00:00'),(5,'Ricardo Alves','ricardo.alves@fazenda.com.br','$2a$10$rrPKOP/6TnNqBCJ9KAJetu/VwQvwOeKM8mkrLnydjq9q7COPJ3/Y2','OPERADOR',1,0,'1bbc2abb-7d0d-4220-b169-789d645af3a9_ricardo.png','2026-06-24 00:00:00'),(6,'Patrícia Souza','patricia.souza@fazenda.com.br','$2a$10$b7FTAM5idt9Cg6AI1WeCkutYG8P5.uSnOwbbduIbZ5gvw8KkLu9wG','PROPRIETARIO',1,0,'47f6ae57-08fd-41b0-8296-477c74b8e884_patricia.png','2026-06-24 00:00:00'),(7,'Roberto Lima','roberto.lima@fazenda.com.br','$2a$10$765YAXKjBIARkU2SsJUBFONbQlsYJYdFRVrhdZU4HXBlaON34SbqC','SOCIO',1,0,'5b0fa8c5-6afe-4273-ad62-e8dace2c757c_roberto.png','2026-07-16 00:00:00');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_maquina`
--

DROP TABLE IF EXISTS `usuario_maquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_maquina` (
  `id_usuario` int NOT NULL,
  `id_maquina` int NOT NULL,
  PRIMARY KEY (`id_usuario`,`id_maquina`),
  KEY `id_maquina` (`id_maquina`),
  CONSTRAINT `usuario_maquina_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `usuario_maquina_ibfk_2` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_maquina`
--

LOCK TABLES `usuario_maquina` WRITE;
/*!40000 ALTER TABLE `usuario_maquina` DISABLE KEYS */;
INSERT INTO `usuario_maquina` VALUES (2,1),(3,2),(2,3),(2,4),(4,4),(4,5),(6,6);
/*!40000 ALTER TABLE `usuario_maquina` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-08 14:53:11
