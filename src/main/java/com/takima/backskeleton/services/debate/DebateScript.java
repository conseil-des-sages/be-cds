package com.takima.backskeleton.services.debate;

import java.util.List;

/** Le déroulé d'un débat : trois tours de parole, puis la synthèse du Greffier. */
public final class DebateScript {
    public static final int SYNTHESIS_ORDER = 4;

    public static final List<Round> ROUNDS = List.of(
            new Round(1, "Ouverture", "Donne ta position initiale sur les options."),
            new Round(2, "Confrontation",
                    "Lis les positions des autres sages. Réponds nommément à au moins l'un d'eux."),
            new Round(3, "Position finale", "Donne ta position finale. Indique si tu as changé d'avis et pourquoi."),
            new Round(SYNTHESIS_ORDER, "Synthèse", "Le Greffier rédige la synthèse du débat."));

    private DebateScript() {
    }

    public record Round(int order, String name, String description) {
    }
}
