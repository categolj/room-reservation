package com.example.reservation.query;

import com.example.reservation.command.CancelReservationCommand;
import com.example.reservation.command.RequestReservationCommand;
import com.example.reservation.command.ReservationCommandHandler;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;

@Service
public class ReservationService {

	private final ReservationCommandHandler reservationCommandHandler;

	private final ReservationViewRepository reservationViewRepository;

	private final IdGenerator idGenerator;

	public ReservationService(ReservationCommandHandler reservationCommandHandler,
			ReservationViewRepository reservationViewRepository, IdGenerator idGenerator) {
		this.reservationCommandHandler = reservationCommandHandler;
		this.reservationViewRepository = reservationViewRepository;
		this.idGenerator = idGenerator;
	}

	@Tool(description = """
			Retrieves a reservation by its unique identifier.
			Returns an Optional containing the reservation view if found, or an empty Optional if the reservation does not exist.
			""")
	public Optional<ReservationView> getReservation(
			@ToolParam(description = "The unique identifier of the reservation to retrieve.") UUID reservationId) {
		return this.reservationViewRepository.findByReservationId(reservationId);
	}

	@Tool(description = """
			Finds all reservations for a specific room on a given date.
			Returns a list of reservation views matching the room ID and date criteria.
			""")
	public List<ReservationView> findByRoomIdAndDate(
			@ToolParam(description = "The unique identifier of the room to find reservations for.") UUID roomId,
			@ToolParam(description = "The date on which to find reservations.") LocalDate date) {
		return this.reservationViewRepository.findByRoomIdAndDate(roomId, date);
	}

	@Tool(description = """
			Creates a new reservation request based on the provided details.
			Returns the newly generated reservation ID.
			""")
	public UUID requestReservation(@ToolParam(
			description = "A record containing all necessary information for the reservation including room ID, date, start time, end time, and purpose.") ReservationRequest request) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		UUID reservationId = this.idGenerator.generateId();
		RequestReservationCommand command = new RequestReservationCommand(reservationId, request.roomId(),
				request.date(), request.startTime(), request.endTime(), request.purpose(), userId);
		this.reservationCommandHandler.handleRequestReservation(command);
		return reservationId;
	}

	@Tool(description = """
			Cancels an existing reservation based on its unique identifier.
			""")
	public void cancelReservation(
			@ToolParam(description = "The unique identifier of the reservation to cancel.") UUID reservationId) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		CancelReservationCommand command = new CancelReservationCommand(reservationId, userId);
		this.reservationCommandHandler.handleCancelReservation(command);
	}

	public record ReservationRequest(UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
			String purpose) {
	}

}
