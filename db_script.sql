CREATE DATABASE  IF NOT EXISTS `frota_agricola` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `frota_agricola`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: frota_agricola
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abastecimento`
--

LOCK TABLES `abastecimento` WRITE;
/*!40000 ALTER TABLE `abastecimento` DISABLE KEYS */;
INSERT INTO `abastecimento` VALUES (1,1,2,'2026-06-20 06:45:00',150.00,'Diesel S10',1240.50),(2,2,3,'2026-06-21 05:30:00',400.00,'Diesel S500',420.00),(3,3,2,'2026-06-22 07:45:00',200.00,'Diesel S10',3200.80);
/*!40000 ALTER TABLE `abastecimento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `maquina`
--

DROP TABLE IF EXISTS `maquina`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `maquina` (
  `id_maquina` int NOT NULL AUTO_INCREMENT,
  `modelo` varchar(100) NOT NULL,
  `ano` int NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `hodometro` decimal(10,2) NOT NULL DEFAULT '0.00',
  `intervalo_manut_km` decimal(10,2) NOT NULL,
  `status` enum('Disponivel','Em Operacao','Em Manutencao','Inativa') DEFAULT 'Disponivel',
  `nivel_risco` enum('Baixo','Medio','Alto') DEFAULT 'Baixo',
  `consumo_medio` decimal(5,2) DEFAULT '0.00',
  `data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_maquina`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `maquina`
--

LOCK TABLES `maquina` WRITE;
/*!40000 ALTER TABLE `maquina` DISABLE KEYS */;
INSERT INTO `maquina` VALUES (1,'Trator John Deere 6100J',2021,'Trator',1250.50,500.00,'Disponivel','Baixo',12.50,'2026-06-23 13:45:24'),(2,'Colheitadeira Case IH 250',2023,'Colheitadeira',450.00,300.00,'Em Operacao','Medio',35.00,'2026-06-23 13:45:24'),(3,'Pulverizador Jacto Uniport 3030',2020,'Pulverizador',3200.80,400.00,'Em Manutencao','Alto',18.20,'2026-06-23 13:45:24');
/*!40000 ALTER TABLE `maquina` ENABLE KEYS */;
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
  `tipo` enum('Preventivo','Anomalia','Risco','Aprovacao') NOT NULL,
  `mensagem` text NOT NULL,
  `lida` tinyint(1) DEFAULT '0',
  `data_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_notificacao`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_maquina` (`id_maquina`),
  CONSTRAINT `notificacao_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `notificacao_ibfk_2` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacao`
--

LOCK TABLES `notificacao` WRITE;
/*!40000 ALTER TABLE `notificacao` DISABLE KEYS */;
INSERT INTO `notificacao` VALUES (1,1,1,'Preventivo','O Trator John Deere está se aproximando do limite de quilometragem para a próxima revisão (faltam 50km).',0,'2026-06-23 13:45:33'),(2,1,2,'Aprovacao','Nova Ordem de Manutenção pendente de aprovação para a Colheitadeira Case IH.',0,'2026-06-23 13:45:33'),(3,2,3,'Anomalia','Falha crítica registrada no Pulverizador Jacto. Máquina marcada como Inativa/Em Manutenção.',1,'2026-06-23 13:45:33');
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
  `status` enum('Aguardando Aprovação','Ativa','Encerrada') NOT NULL,
  `prioridade` enum('Baixa','Media','Alta','Critica') NOT NULL,
  `descricao` text NOT NULL,
  `observacao_encerramento` text,
  `data_abertura` datetime DEFAULT CURRENT_TIMESTAMP,
  `data_encerramento` datetime DEFAULT NULL,
  PRIMARY KEY (`id_ordem`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_solicitante` (`id_solicitante`),
  CONSTRAINT `ordem_manutencao_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `ordem_manutencao_ibfk_2` FOREIGN KEY (`id_solicitante`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordem_manutencao`
--

LOCK TABLES `ordem_manutencao` WRITE;
/*!40000 ALTER TABLE `ordem_manutencao` DISABLE KEYS */;
INSERT INTO `ordem_manutencao` VALUES (1,1,2,'Encerrada','Baixa','Troca de óleo do motor e filtros.','Serviço realizado pela oficina parceira.','2026-05-10 10:00:00','2026-05-11 15:00:00'),(2,2,1,'Aguardando Aprovação','Media','Revisão das correias e rolamentos antes da safra.',NULL,'2026-06-23 09:00:00',NULL),(3,3,2,'Ativa','Alta','Substituição da bomba de pulverização estourada.',NULL,'2026-06-22 09:30:00',NULL);
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
  PRIMARY KEY (`id_operacao`),
  KEY `id_maquina` (`id_maquina`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `registro_operacao_ibfk_1` FOREIGN KEY (`id_maquina`) REFERENCES `maquina` (`id_maquina`) ON DELETE RESTRICT,
  CONSTRAINT `registro_operacao_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registro_operacao`
--

LOCK TABLES `registro_operacao` WRITE;
/*!40000 ALTER TABLE `registro_operacao` DISABLE KEYS */;
INSERT INTO `registro_operacao` VALUES (1,1,2,'2026-06-20 07:00:00','2026-06-20 17:00:00',1240.50,1250.50,NULL,'Gradeação do talhão 4.'),(2,2,3,'2026-06-21 06:00:00','2026-06-21 18:00:00',420.00,450.00,15000.00,'Colheita de soja no talhão 2.'),(3,3,2,'2026-06-22 08:00:00',NULL,3200.80,NULL,NULL,'Operação interrompida por falha na bomba de pulverização.');
/*!40000 ALTER TABLE `registro_operacao` ENABLE KEYS */;
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
  `senha_hash` varchar(255) NOT NULL,
  `perfil` enum('PROPRIETARIO','OPERADOR') NOT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  `data_criacao` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'João Batista','joao.batista@fazenda.com.br','hash_senha_exemplo_123','PROPRIETARIO',1,'2026-06-23 13:45:21'),(2,'Carlos Mendes','carlos.mendes@fazenda.com.br','hash_senha_exemplo_456','OPERADOR',1,'2026-06-23 13:45:21'),(3,'Marcos Silva','marcos.silva@fazenda.com.br','hash_senha_exemplo_789','OPERADOR',1,'2026-06-23 13:45:21');
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
INSERT INTO `usuario_maquina` VALUES (2,1),(3,2),(2,3);
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

-- Dump completed on 2026-06-25 13:59:43
