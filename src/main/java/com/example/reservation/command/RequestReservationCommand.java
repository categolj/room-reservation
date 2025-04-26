package com.example.reservation.command;

import am.ik.yavi.arguments.Arguments7Validator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static com.example.reservation.command.Reservation.reservationValidator;

/**
 * Command for requesting a new reservation
 */
public record RequestReservationCommand(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime,
		LocalTime endTime, String purpose, UUID userId) {

	private static final Arguments7Validator<UUID, UUID, LocalDate, LocalTime, LocalTime, String, UUID, RequestReservationCommand> validator = reservationValidator
		.andThen(reservation -> new RequestReservationCommand(reservation.getReservationId(), reservation.getRoomId(),
				reservation.getDate(), reservation.getStartTime(), reservation.getEndTime(), reservation.getPurpose(),
				reservation.getUserId()));

	public RequestReservationCommand {
		validator.lazy().validated(reservationId, roomId, date, startTime, endTime, purpose, userId);
	}
}