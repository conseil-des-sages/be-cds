-- =====================================================================
-- Le Conseil des Sages - schéma MySQL 8 (v2)
--
-- Héritage : participant (mère) -> user, sage (filles, même identifiant)
-- Une session regroupe des participants (sages + joueur) via participation,
-- se découpe en steps (rounds), et chaque step contient des messages.
--
-- Convention de nommage : anglais, <entite>_<attribut>, clés en <entite>_id
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS conseil_des_sages
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE conseil_des_sages;

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------

CREATE TABLE role (
                      role_id   INT         NOT NULL AUTO_INCREMENT,
                      role_name VARCHAR(50) NOT NULL,
                      PRIMARY KEY (role_id),
                      UNIQUE KEY uq_role_name (role_name)
) ENGINE = InnoDB;

-- Entité mère : tout ce qui peut parler (joueur ou sage)
CREATE TABLE participant (
                             participant_id         INT                  NOT NULL AUTO_INCREMENT,
                             participant_name       VARCHAR(100)         NOT NULL,   -- pseudo du joueur ou nom du sage
                             participant_created_at DATETIME             NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             participant_updated_at DATETIME                 NULL ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (participant_id)
) ENGINE = InnoDB;

-- Fille : le joueur (seul à pouvoir se connecter)
CREATE TABLE `user` (
                        participant_id     INT          NOT NULL,
                        user_email         VARCHAR(255) NOT NULL,
                        user_password_hash VARCHAR(255) NOT NULL,               -- hash (bcrypt/argon2), jamais en clair
                        user_is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
                        user_last_login_at DATETIME         NULL,
                        role_id            INT          NOT NULL,
                        PRIMARY KEY (participant_id),
                        UNIQUE KEY uq_user_email (user_email),
                        CONSTRAINT fk_user_participant FOREIGN KEY (participant_id)
                            REFERENCES participant (participant_id) ON DELETE CASCADE,
                        CONSTRAINT fk_user_role FOREIGN KEY (role_id)
                            REFERENCES role (role_id)
) ENGINE = InnoDB;

-- Fille : le sage (pas de compte de connexion)
CREATE TABLE sage (
                      participant_id     INT          NOT NULL,
                      sage_model         VARCHAR(100) NOT NULL,               -- identifiant du modèle LLM utilisé
                      sage_temperature   FLOAT        NOT NULL,
                      sage_max_tokens    INT          NOT NULL DEFAULT 1000,
                      sage_system_prompt TEXT         NOT NULL,
                      sage_description   VARCHAR(500)     NULL,               -- texte affiché au joueur
                      sage_is_public     BOOLEAN      NOT NULL DEFAULT FALSE, -- partagé avec les autres joueurs
                      creator_id         INT              NULL,               -- NULL = sage de base, sinon joueur propriétaire
                      PRIMARY KEY (participant_id),
                      CONSTRAINT ck_sage_temperature CHECK (sage_temperature BETWEEN 0 AND 1),
                      CONSTRAINT ck_sage_max_tokens  CHECK (sage_max_tokens > 0),
                      CONSTRAINT fk_sage_participant FOREIGN KEY (participant_id)
                          REFERENCES participant (participant_id) ON DELETE CASCADE,
                      CONSTRAINT fk_sage_creator FOREIGN KEY (creator_id)
                          REFERENCES `user` (participant_id)
) ENGINE = InnoDB;

CREATE TABLE `session` (
                           session_id         INT                                       NOT NULL AUTO_INCREMENT,
                           session_name       VARCHAR(150)                              NOT NULL,
                           session_status     ENUM('IN_PROGRESS', 'FINISHED', 'ARCHIVED') NOT NULL DEFAULT 'IN_PROGRESS',
                           session_created_at DATETIME                                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           session_updated_at DATETIME                                      NULL ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (session_id)
) ENGINE = InnoDB;

-- Composition du conseil : qui participe à quelle session, avec quel rôle
CREATE TABLE participation (
                               session_id              INT                      NOT NULL,
                               participant_id          INT                      NOT NULL,
                               participation_joined_at DATETIME                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (session_id, participant_id),
                               CONSTRAINT fk_participation_session FOREIGN KEY (session_id)
                                   REFERENCES `session` (session_id) ON DELETE CASCADE,
                               CONSTRAINT fk_participation_participant FOREIGN KEY (participant_id)
                                   REFERENCES participant (participant_id)
) ENGINE = InnoDB;

-- Un step = un round d'une session (3 par défaut, on peut en ajouter)
CREATE TABLE step (
                      step_id          INT                                   NOT NULL AUTO_INCREMENT,
                      step_order       INT                                   NOT NULL,
                      step_name        VARCHAR(100)                          NOT NULL,
                      step_description VARCHAR(500)                          NOT NULL,  -- consigne ajoutée au prompt
                      step_status      ENUM('PENDING', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'PENDING',
                      step_created_at  DATETIME                              NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      step_updated_at  DATETIME                                  NULL ON UPDATE CURRENT_TIMESTAMP,
                      session_id       INT                                   NOT NULL,
                      PRIMARY KEY (step_id),
                      UNIQUE KEY uq_step_order (session_id, step_order),
                      CONSTRAINT fk_step_session FOREIGN KEY (session_id)
                          REFERENCES `session` (session_id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- Les messages sont rangés par step (donc par round et par session)
CREATE TABLE message (
                         message_id          INT          NOT NULL AUTO_INCREMENT,
                         message_content     TEXT         NOT NULL,
                         message_order       INT          NOT NULL,              -- rang du message dans son step
                         message_token_count INT              NULL,              -- rempli pour les messages générés par un sage
                         message_model       VARCHAR(100)     NULL,              -- modèle effectivement utilisé pour générer le message
                         message_created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         message_updated_at  DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                         message_deleted_at  DATETIME         NULL,              -- suppression logique
                         participant_id      INT          NOT NULL,              -- auteur : sage ou joueur
                         step_id             INT          NOT NULL,
                         parent_message_id   INT              NULL,              -- message auquel celui-ci répond (facultatif)
                         PRIMARY KEY (message_id),
                         UNIQUE KEY uq_message_order (step_id, message_order),
                         CONSTRAINT ck_message_token_count CHECK (message_token_count IS NULL OR message_token_count >= 0),
                         CONSTRAINT fk_message_participant FOREIGN KEY (participant_id)
                             REFERENCES participant (participant_id),
                         CONSTRAINT fk_message_step FOREIGN KEY (step_id)
                             REFERENCES step (step_id) ON DELETE CASCADE,
                         CONSTRAINT fk_message_parent FOREIGN KEY (parent_message_id)
                             REFERENCES message (message_id) ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Index utiles (les clés primaires et uniques sont déjà indexées)
-- ---------------------------------------------------------------------

CREATE INDEX idx_user_role             ON `user`        (role_id);
CREATE INDEX idx_sage_creator          ON sage          (creator_id);
CREATE INDEX idx_participation_part    ON participation (participant_id);
CREATE INDEX idx_message_participant   ON message       (participant_id);
CREATE INDEX idx_message_parent        ON message       (parent_message_id);