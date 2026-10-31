package com.takima.backskeleton.DTO;

import java.util.List;

public record StatsDto(long totalDebates, long debatesThisWeek, List<String> themes) {
}
