package com.takima.backskeleton.services.debate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SynthesisParserTest {
    private final SynthesisParser parser = new SynthesisParser(new ObjectMapper());

    @Test
    void readsJsonWrappedInMarkdown() {
        JsonNode synthesis = parser.parse("Voici :\n```json\n{\"verdict\":\"Lyon\",\"theme\":\"Emploi\"}\n```").orElseThrow();

        assertThat(synthesis.path("verdict").asText()).isEqualTo("Lyon");
        assertThat(parser.theme(synthesis)).isEqualTo("Emploi");
    }

    @Test
    void rejectsAnythingWithoutVerdict() {
        assertThat(parser.parse("{\"summary\":\"x\"}")).isEmpty();
        assertThat(parser.parse("pas de json")).isEmpty();
        assertThat(parser.parse("{cassé")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void unknownThemeBecomesOther() {
        JsonNode synthesis = parser.parse("{\"verdict\":\"v\",\"theme\":\"Astrologie\"}").orElseThrow();

        assertThat(parser.theme(synthesis)).isEqualTo(SynthesisParser.OTHER_THEME);
    }
}
