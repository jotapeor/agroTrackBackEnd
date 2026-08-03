-- Patch: cria tabela historico_status_maquina e adiciona coluna peso_final
-- Executar manualmente no editor SQL do MySQL Workbench

USE agrotrack_db;

CREATE TABLE IF NOT EXISTS `historico_status_maquina` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `id_maquina` INT NOT NULL,
    `id_usuario` INT NOT NULL,
    `status_anterior` VARCHAR(50) NOT NULL,
    `novo_status` VARCHAR(50) NOT NULL,
    `motivo` TEXT NOT NULL,
    `data_alteracao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_hsm_maquina` FOREIGN KEY (`id_maquina`) REFERENCES `maquina`(`id_maquina`) ON DELETE RESTRICT,
    CONSTRAINT `fk_hsm_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario`(`id_usuario`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS _add_peso_final;
DELIMITER //
CREATE PROCEDURE _add_peso_final()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'registro_operacao'
          AND COLUMN_NAME  = 'peso_final'
    ) THEN
        ALTER TABLE registro_operacao
            ADD COLUMN peso_final DECIMAL(10,2) NULL
            COMMENT 'Peso colhido ao encerrar (apenas Colheitadeira)';
    END IF;
END //
DELIMITER ;
CALL _add_peso_final();
DROP PROCEDURE IF EXISTS _add_peso_final;
