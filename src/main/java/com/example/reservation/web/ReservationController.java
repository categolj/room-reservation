package com.example.reservation.web;

import com.example.reservation.command.CancelReservationCommand;
import com.example.reservation.command.RequestReservationCommand;
import com.example.reservation.command.ReservationCommandHandler;
import com.example.reservation.query.ReservationView;
import com.example.reservation.query.ReservationViewRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ReservationController {

	private final ReservationCommandHandler reservationCommandHandler;

	private final ReservationViewRepository reservationViewRepository;

	private final IdGenerator idGenerator;

	public ReservationController(ReservationCommandHandler reservationCommandHandler,
			ReservationViewRepository reservationViewRepository, IdGenerator idGenerator) {
		this.reservationCommandHandler = reservationCommandHandler;
		this.reservationViewRepository = reservationViewRepository;
		this.idGenerator = idGenerator;
	}

	@GetMapping(path = "/api/reservations/{reservationId}")
	public ResponseEntity<ReservationView> getReservation(@PathVariable UUID reservationId) {
		return ResponseEntity.of(this.reservationViewRepository.findByReservationId(reservationId));
	}

	@GetMapping(path = "/api/reservations")
	public List<ReservationView> findReservation(@RequestParam UUID roomId, @RequestParam LocalDate date) {
		return this.reservationViewRepository.findByRoomIdAndDate(roomId, date);
	}

	@PostMapping(path = "/api/reservations")
	public ResponseEntity<Void> requestReservation(@RequestBody ReservationRequest request,
			UriComponentsBuilder uriBuilder) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		UUID reservationId = this.idGenerator.generateId();
		RequestReservationCommand command = new RequestReservationCommand(reservationId, request.roomId(),
				request.date(), request.startTime(), request.endTime(), request.purpose(), userId);
		this.reservationCommandHandler.handleRequestReservation(command);
		return ResponseEntity.created(uriBuilder.path("/api/reservations/{reservationId}").build(reservationId))
			.build();
	}

	@DeleteMapping(path = "/api/reservations/{reservationId}")
	public ResponseEntity<Void> cancelReservation(@PathVariable UUID reservationId) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		CancelReservationCommand command = new CancelReservationCommand(reservationId, userId);
		this.reservationCommandHandler.handleCancelReservation(command);
		return ResponseEntity.noContent().build();
	}

	public record ReservationRequest(UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
			String purpose) {
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleIllegalStateException(IllegalStateException e) {
		return new ErrorResponse(e.getMessage());
	}

}
