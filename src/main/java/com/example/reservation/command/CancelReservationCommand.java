package com.example.reservation.command;

import java.util.UUID;

/**
 * Command for canceling an existing reservation
 */
public record CancelReservationCommand(UUID reservationId, UUID userId) {
}