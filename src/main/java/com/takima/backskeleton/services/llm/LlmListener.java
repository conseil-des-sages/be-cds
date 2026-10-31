package com.takima.backskeleton.services.llm;

/** Suit une génération en cours. */
public interface LlmListener {
    void onDelta(String delta);

    /** Une tentative a échoué : le texte déjà reçu est à oublier, une autre va commencer. */
    void onRetry();
}
