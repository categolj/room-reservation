package com.example.availability.query;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityView(UUID roomId, String roomName, LocalDate date) {
}
