package com.takima.backskeleton.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class MeDtos {
    private MeDtos() {
    }

    public record Profile(
            Integer id,
            String name,
            String email,
            String role,
            LocalDateTime createdAt,
            long debates,
            long sages
    ) {
    }

    /** PUT /api/me/council */
    public record CouncilRequest(
            @NotNull(message = "Choisis les sages du conseil.")
            List<Integer> sageIds
    ) {
    }

    public record CouncilView(List<Integer> sageIds, int min, int max) {
    }
}
