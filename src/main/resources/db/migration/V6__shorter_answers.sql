-- Des réponses plus courtes rendent le débat plus vif.
UPDATE sage SET sage_system_prompt = replace(sage_system_prompt, '120 mots', '80 mots')
WHERE creator_id IS NULL AND sage_kind = 'DEBATER';
