package com.example.reservation.command;

import am.ik.yavi.arguments.Arguments2Validator;
import java.util.UUID;

import static com.example.reservation.command.Reservation.reservationIdValidator;

/**
 * Command for canceling an existing reservation
 */
public record CancelReservationCommand(UUID reservationId, UUID userId) {
	private static final Arguments2Validator<UUID, UUID, CancelReservationCommand> validator = reservationIdValidator
		.split(Reservation.userIdValidator)
		.apply(CancelReservationCommand::new);

	public CancelReservationCommand {
		validator.lazy().validated(reservationId, userId);
	}
}