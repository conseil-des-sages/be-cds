-- =====================================================================
-- Moteur de débat : une session appartient à un joueur, porte sa question,
-- et les sages ont une apparence, une visibilité et un type.
-- =====================================================================

ALTER TABLE "session"
    ADD COLUMN session_question TEXT,
    ADD COLUMN session_context  TEXT,
    ADD COLUMN session_theme    VARCHAR(40),
    ADD COLUMN session_owner_id INT,
    ADD CONSTRAINT fk_session_owner FOREIGN KEY (session_owner_id)
        REFERENCES "user" (participant_id) ON DELETE CASCADE;

UPDATE "session" SET session_question = session_name WHERE session_question IS NULL;
ALTER TABLE "session" ALTER COLUMN session_question SET NOT NULL;

UPDATE "session" s
SET session_owner_id = p.participant_id
FROM participation p
         JOIN "user" u ON u.participant_id = p.participant_id
WHERE p.session_id = s.session_id;

CREATE INDEX idx_session_owner ON "session" (session_owner_id, session_created_at DESC);

ALTER TABLE sage
    ADD COLUMN sage_short_name VARCHAR(40),
    ADD COLUMN sage_color      VARCHAR(20) NOT NULL DEFAULT 'futur',
    ADD COLUMN sage_icon       VARCHAR(40) NOT NULL DEFAULT 'sparkles',
    ADD COLUMN sage_kind       VARCHAR(20) NOT NULL DEFAULT 'DEBATER'
        CONSTRAINT ck_sage_kind CHECK (sage_kind IN ('DEBATER', 'SCRIBE')),
    ADD COLUMN sage_is_public  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN sage_is_default BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN sage_deleted_at TIMESTAMP;

-- Rang du participant autour de la table : l'ordre de parole
ALTER TABLE participation ADD COLUMN participation_seat INT NOT NULL DEFAULT 0;

-- Le conseil que le joueur a composé, réutilisé à chaque nouveau débat
CREATE TABLE user_council (
    user_id INT NOT NULL,
    sage_id INT NOT NULL,
    seat    INT NOT NULL,
    PRIMARY KEY (user_id, sage_id),
    CONSTRAINT fk_user_council_user FOREIGN KEY (user_id)
        REFERENCES "user" (participant_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_council_sage FOREIGN KEY (sage_id)
        REFERENCES sage (participant_id) ON DELETE CASCADE
);
