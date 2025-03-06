package com.example.reservation.query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationView(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
		String purpose, UUID userId) {
}