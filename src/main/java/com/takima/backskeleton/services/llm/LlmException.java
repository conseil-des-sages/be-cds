package com.takima.backskeleton.services.llm;

/** Un fournisseur n'a pas pu répondre. {@code retryable} : la même requête peut réussir un peu plus tard. */
public class LlmException extends RuntimeException {
    private final boolean retryable;

    public LlmException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public LlmException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
