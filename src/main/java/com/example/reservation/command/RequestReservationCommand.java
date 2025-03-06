package com.example.reservation.command;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Command for requesting a new reservation
 */
public record RequestReservationCommand(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime,
		LocalTime endTime, String purpose, UUID userId) {
}