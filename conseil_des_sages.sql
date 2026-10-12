---------------------------------------------------
-- MySQL - Conseil des Sages (avec héritage)
-- Participant (mère) -> User et Sage (filles)
---------------------------------------------------

CREATE TABLE role (
    role_id INT NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id)
);

-- Entité mère : tout ce qui peut parler (joueur ou sage)
CREATE TABLE participant (
    id_participant INT NOT NULL AUTO_INCREMENT,
    nom_participant VARCHAR(100) NOT NULL,          -- pseudo du joueur ou nom du sage
    created_at_participant DATETIME NOT NULL,
    updated_at_participant DATETIME,
    PRIMARY KEY (id_participant)
);

-- Fille 1 : le joueur (compte de connexion)
CREATE TABLE user (
    id_participant INT NOT NULL,
    email_user VARCHAR(255) NOT NULL,
    password_user VARCHAR(255) NOT NULL,            -- hash, jamais en clair
    role_id INT NOT NULL,
    PRIMARY KEY (id_participant),
    UNIQUE (email_user)
);

-- Fille 2 : le sage (pas de compte de connexion)
CREATE TABLE sage (
    id_participant INT NOT NULL,
    temperature_sage FLOAT NOT NULL,
    prompt_systeme_sage TEXT NOT NULL,
    id_createur INT,                                -- NULL = sage de base, sinon joueur propriétaire
    PRIMARY KEY (id_participant)
);

CREATE TABLE session (
    id_session INT NOT NULL AUTO_INCREMENT,
    created_at_session DATETIME NOT NULL,
    updated_at_session DATETIME,
    id_user INT NOT NULL,                           -- propriétaire (1,1), remplace to_possess
    PRIMARY KEY (id_session)
);

-- Un step = un round d'une session (3 par défaut, on peut en ajouter)
CREATE TABLE step (
    step_id INT NOT NULL AUTO_INCREMENT,
    step_ordre INT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_description VARCHAR(500) NOT NULL,
    step_created_at DATETIME NOT NULL,
    id_session INT NOT NULL,
    PRIMARY KEY (step_id),
    UNIQUE (id_session, step_ordre)
);

-- Les messages sont rangés par step (donc par round et par session)
CREATE TABLE message (
    message_id INT NOT NULL AUTO_INCREMENT,
    message_content TEXT NOT NULL,
    message_created_at DATETIME NOT NULL,
    id_participant INT NOT NULL,                    -- auteur : sage ou joueur
    step_id INT NOT NULL,
    PRIMARY KEY (message_id)
);

---------------------------------------------------
-- Clés étrangères
---------------------------------------------------

-- Héritage
ALTER TABLE user ADD CONSTRAINT frk_user_participant FOREIGN KEY (id_participant) REFERENCES participant(id_participant) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE sage ADD CONSTRAINT frk_sage_participant FOREIGN KEY (id_participant) REFERENCES participant(id_participant) ON UPDATE NO ACTION ON DELETE CASCADE;

-- Rôle du joueur
ALTER TABLE user ADD CONSTRAINT frk_user_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Créateur du sage (nullable)
ALTER TABLE sage ADD CONSTRAINT frk_sage_createur FOREIGN KEY (id_createur) REFERENCES user(id_participant) ON UPDATE NO ACTION ON DELETE CASCADE;

-- Session -> propriétaire
ALTER TABLE session ADD CONSTRAINT frk_session_user FOREIGN KEY (id_user) REFERENCES user(id_participant) ON UPDATE NO ACTION ON DELETE CASCADE;

-- Message -> auteur, round, session
ALTER TABLE message ADD CONSTRAINT frk_message_participant FOREIGN KEY (id_participant) REFERENCES participant(id_participant) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE message ADD CONSTRAINT frk_message_step FOREIGN KEY (step_id) REFERENCES step(step_id) ON UPDATE NO ACTION ON DELETE CASCADE;

-- Step -> session
ALTER TABLE step ADD CONSTRAINT frk_step_session FOREIGN KEY (id_session) REFERENCES session(id_session) ON UPDATE NO ACTION ON DELETE CASCADE;
