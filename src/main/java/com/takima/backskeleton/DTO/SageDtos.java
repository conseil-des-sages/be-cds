package com.takima.backskeleton.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SageDtos {
    private SageDtos() {
    }

    /** POST /api/sages et PUT /api/sages/{id} */
    public record UpsertRequest(
            @NotBlank(message = "Le nom est obligatoire.")
            @Size(min = 2, max = 40, message = "Le nom fait entre 2 et 40 caractères.")
            String name,
            @NotBlank(message = "La description est obligatoire.")
            @Size(max = 500, message = "La description fait 500 caractères maximum.")
            String description,
            @NotBlank(message = "La personnalité du sage est obligatoire.")
            @Size(min = 10, max = 4000, message = "Le prompt fait entre 10 et 4000 caractères.")
            String prompt,
            @NotBlank(message = "Le modèle est obligatoire.")
            String model,
            @NotNull(message = "La température est obligatoire.")
            @DecimalMin(value = "0.0", message = "La température est comprise entre 0 et 1.")
            @DecimalMax(value = "1.0", message = "La température est comprise entre 0 et 1.")
            Float temperature,
            @NotNull(message = "La longueur des réponses est obligatoire.")
            @Min(value = 100, message = "Il faut au moins 100 tokens.")
            @Max(value = 2000, message = "2000 tokens maximum.")
            Integer maxTokens,
            @NotBlank(message = "La couleur est obligatoire.")
            @Pattern(regexp = "prudent|optimiste|financier|avocat|futur|cyan|orange|indigo",
                    message = "Cette couleur n'existe pas.")
            String color,
            @NotBlank(message = "L'icône est obligatoire.")
            @Pattern(regexp = "[a-z-]{1,40}", message = "Cette icône n'existe pas.")
            String icon,
            boolean shared
    ) {
    }

    /** prompt, model, temperature et maxTokens ne sont renseignés que pour le créateur du sage. */
    public record View(
            Integer id,
            String name,
            String shortName,
            String description,
            String color,
            String icon,
            String origin,
            String creator,
            boolean shared,
            boolean inDefaultCouncil,
            long uses,
            String prompt,
            String model,
            Float temperature,
            Integer maxTokens
    ) {
    }
}
