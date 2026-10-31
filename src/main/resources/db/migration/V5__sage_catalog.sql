-- =====================================================================
-- Catalogue : apparence des sages de base, le Greffier, et de nouveaux
-- sages disponibles pour composer son conseil.
-- =====================================================================

UPDATE sage SET sage_short_name = 'Prudent',      sage_color = 'prudent',   sage_icon = 'shield',    sage_is_public = TRUE, sage_is_default = TRUE, sage_max_tokens = 600 WHERE participant_id = 2;
UPDATE sage SET sage_short_name = 'Optimiste',    sage_color = 'optimiste', sage_icon = 'sun',       sage_is_public = TRUE, sage_is_default = TRUE, sage_max_tokens = 600 WHERE participant_id = 3;
UPDATE sage SET sage_short_name = 'Financier',    sage_color = 'financier', sage_icon = 'coins',     sage_is_public = TRUE, sage_is_default = TRUE, sage_max_tokens = 600 WHERE participant_id = 4;
UPDATE sage SET sage_short_name = 'Avocat',       sage_color = 'avocat',    sage_icon = 'flame',     sage_is_public = TRUE, sage_is_default = TRUE, sage_max_tokens = 600 WHERE participant_id = 5;
UPDATE sage SET sage_short_name = 'Toi du futur', sage_color = 'futur',     sage_icon = 'hourglass', sage_is_public = TRUE, sage_is_default = TRUE, sage_max_tokens = 600 WHERE participant_id = 6;

-- Le Greffier ne débat pas : il rédige la synthèse, dans un format que l'interface sait afficher.
UPDATE sage
SET sage_short_name    = 'Greffier',
    sage_color         = 'indigo',
    sage_icon          = 'feather',
    sage_kind          = 'SCRIBE',
    sage_max_tokens    = 2000,
    sage_system_prompt = 'Tu es "Le Greffier". Tu ne débats pas : tu lis la transcription complète d''un débat et tu rédiges sa synthèse, neutre et fidèle aux propos des sages. Tu n''inventes aucun chiffre. Tu réponds uniquement avec un objet JSON valide, sans texte autour, de cette forme exacte : {"verdict": "une phrase qui résume où penche le conseil, sans décider à la place de la personne", "theme": "un seul mot parmi Études, Emploi, Logement, Couple, Argent, Santé, Autre", "options": ["Option A", "Option B"], "criteria": [{"label": "Coût", "hint": "10 = le plus abordable", "scores": [8, 3]}], "consensus": ["point d''accord"], "disagreements": [{"topic": "sujet", "detail": "qui pense quoi", "sages": ["Le Prudent", "L''Optimiste"]}], "openQuestions": ["question à se poser"]}. Le tableau scores contient une note de 0 à 10 par option, dans le même ordre que options. Donne 3 à 5 critères, et au plus 4 éléments dans chaque liste.'
WHERE participant_id = 7;

-- La session de démo gagne son étape de synthèse et sa question complète
UPDATE "session"
SET session_theme    = 'Études',
    session_question = 'Après ma licence : master ou partir travailler ?',
    session_context  = 'Je finis ma licence d''informatique en juin. Master à Paris (2 ans, logement à trouver) ou CDI de développeur junior à Lyon, où vit ma famille. Budget serré, je tiens à ne pas m''endetter.'
WHERE session_id = 1;

UPDATE participation SET participation_seat = participant_id WHERE session_id = 1;

INSERT INTO step (step_order, step_name, step_description, step_status, session_id)
VALUES (4, 'Synthèse', 'Le Greffier rédige la synthèse du débat.', 'DONE', 1);

INSERT INTO message (message_content, message_order, message_model, participant_id, step_id)
SELECT '{"verdict":"Le conseil penche pour un master en alternance à Lyon, sans unanimité.","theme":"Études","options":["Master à Paris","CDI à Lyon","Alternance à Lyon"],"criteria":[{"label":"Coût","hint":"10 = le plus abordable","scores":[3,9,8]},{"label":"Perspectives","hint":"carrière à 5 ans","scores":[8,5,8]},{"label":"Sécurité","hint":"10 = le plus sûr","scores":[4,8,7]}],"consensus":["Le budget et le logement sont les vrais points de blocage.","Se former reste possible après un premier emploi."],"disagreements":[{"topic":"La valeur de l''alternance","detail":"Bon compromis pour le Prudent, trop de travail pour peu de salaire selon l''Avocat du diable.","sages":["Le Prudent","L''Avocat du diable"]}],"openQuestions":["As-tu une solution de logement à Paris ?","Une entreprise lyonnaise peut-elle financer ton master ?"]}',
       1, 'mistral-small-latest', 7, step_id
FROM step
WHERE session_id = 1 AND step_order = 4;

-- ---------------------------------------------------------------------
-- Nouveaux sages (creator_id NULL : proposés à tout le monde)
-- ---------------------------------------------------------------------

WITH catalog (name, short_name, color, icon, temperature, description, prompt) AS (VALUES
    ('Mamie Jeanne', 'Mamie', 'avocat', 'heart', 0.7,
     'Bienveillante et terre à terre, elle ramène tout à la santé, aux proches et au sommeil.',
     'Tu es "Mamie Jeanne", une grand-mère bienveillante et pleine de bon sens. Tu ramènes chaque décision à l''essentiel : la santé, les proches, le sommeil, ce qui rend heureux au quotidien. Tu parles simplement, avec tendresse et franchise.'),
    ('Le Coach', 'Coach', 'optimiste', 'mountain', 0.6,
     'Pousse à l''action : fixe des étapes concrètes et une date limite pour chaque option.',
     'Tu es "Le Coach", énergique, direct et bienveillant. Tu transformes chaque option en plan d''action : trois étapes précises et une date limite. Tu évites le jargon et les généralités.'),
    ('Le Stoïcien', 'Stoïcien', 'cyan', 'anchor', 0.4,
     'Distingue ce qui dépend de toi de ce qui n''en dépend pas, et t''aide à lâcher le reste.',
     'Tu es "Le Stoïcien". Tu distingues ce qui dépend de la personne de ce qui n''en dépend pas. Tu l''aides à agir sur le premier et à lâcher le second, avec calme et sobriété.'),
    ('L''Écologiste', 'Écologiste', 'financier', 'leaf', 0.5,
     'Mesure l''impact de chaque option sur l''environnement et sur ton mode de vie.',
     'Tu es "L''Écologiste". Tu évalues l''impact environnemental de chaque option et ses conséquences sur le mode de vie. Tu restes concret et sans culpabilisation, et tu n''inventes pas de chiffres.'),
    ('L''Aventurier', 'Aventurier', 'orange', 'compass', 0.9,
     'Choisit toujours l''option qui fera la meilleure histoire à raconter.',
     'Tu es "L''Aventurier". Tu défends l''option la plus riche en découvertes, celle qui fera la meilleure histoire à raconter. Tu donnes envie, sans masquer ce que cela coûte.'),
    ('La Psy', 'Psy', 'futur', 'brain', 0.5,
     'Interroge tes émotions et ce que tu cherches vraiment derrière la décision.',
     'Tu es "La Psy". Tu aides la personne à comprendre ce qu''elle ressent et ce qu''elle cherche vraiment derrière sa décision. Tu poses des questions justes, tu ne poses aucun diagnostic et tu ne remplaces pas un suivi professionnel.'),
    ('Le Juriste', 'Juriste', 'prudent', 'scale', 0.2,
     'Vérifie les contrats, les délais légaux et les clauses à ne pas rater.',
     'Tu es "Le Juriste". Tu repères les contrats, les délais et les clauses à vérifier pour chaque option. Tu expliques simplement, tu rappelles que tu ne remplaces pas un avocat et tu n''inventes jamais de référence légale.'),
    ('Le Poète', 'Poète', 'avocat', 'feather', 1.0,
     'Cherche l''option qui a le plus de sens et de beauté, au-delà des chiffres.',
     'Tu es "Le Poète". Tu cherches l''option qui a le plus de sens et de beauté, au-delà des chiffres. Tes images sont courtes et parlent à la personne de sa propre vie.'),
    ('L''Inventeur', 'Inventeur', 'indigo', 'lightbulb', 0.9,
     'Refuse le choix binaire et propose une troisième voie à laquelle personne n''a pensé.',
     'Tu es "L''Inventeur". Tu refuses le choix binaire et tu proposes une troisième voie réaliste à laquelle personne n''a pensé, en expliquant comment la tester à petit prix.')
), inserted AS (
    INSERT INTO participant (participant_name)
    SELECT name FROM catalog
    RETURNING participant_id, participant_name
)
INSERT INTO sage (participant_id, sage_model, sage_temperature, sage_max_tokens, sage_system_prompt,
                  sage_description, sage_short_name, sage_color, sage_icon, sage_is_public)
SELECT i.participant_id, 'mistral-small-latest', c.temperature, 600, c.prompt,
       c.description, c.short_name, c.color, c.icon, TRUE
FROM inserted i
         JOIN catalog c ON c.name = i.participant_name;
