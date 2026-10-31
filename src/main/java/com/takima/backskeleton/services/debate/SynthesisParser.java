package com.takima.backskeleton.services.debate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/** Lit la synthèse du Greffier, en tolérant ce que les modèles ajoutent parfois autour du JSON. */
@Component
public class SynthesisParser {
    public static final String OTHER_THEME = "Autre";
    private static final Set<String> THEMES = Set.of("Études", "Emploi", "Logement", "Couple", "Argent", "Santé", OTHER_THEME);

    private final ObjectMapper mapper;

    public SynthesisParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<JsonNode> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return Optional.empty();
        }
        try {
            JsonNode node = mapper.readTree(raw.substring(start, end + 1));
            return node.path("verdict").isTextual() ? Optional.of(node) : Optional.empty();
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public String theme(JsonNode synthesis) {
        String theme = synthesis.path("theme").asText("");
        return THEMES.contains(theme) ? theme : OTHER_THEME;
    }
}
