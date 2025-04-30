package com.example.reservation.web;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.core.ConstraintViolations;
import com.example.reservation.query.ReservationService;
import com.example.reservation.query.ReservationService.ReservationRequest;
import com.example.reservation.query.ReservationView;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	private final ReservationService reservationService;

	private final Arguments1Validator<Map<String, String>, ReservationRequest> reservationRequestParser = ReservationRequest.parser
		.compose(req -> Arguments.of(req.get("roomId"), req.get("date"), req.get("startTime"), req.get("endTime"),
				req.get("purpose")));

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@GetMapping(path = "/api/reservations/{reservationId}")
	public ResponseEntity<ReservationView> getReservation(@PathVariable UUID reservationId) {
		return ResponseEntity.of(this.reservationService.getReservation(reservationId));
	}

	@GetMapping(path = "/api/reservations")
	public List<ReservationView> findReservation(@RequestParam UUID roomId, @RequestParam LocalDate date) {
		return this.reservationService.findByRoomIdAndDate(roomId, date);
	}

	@PostMapping(path = "/api/reservations")
	public ResponseEntity<?> requestReservation(@RequestBody Map<String, String> req, UriComponentsBuilder uriBuilder) {
		return reservationRequestParser.validate(req)
			.fold(violations -> ResponseEntity.badRequest()
				.body(new ErrorResponse("Constraint violations found!", ConstraintViolations.of(violations))),
					request -> {
						UUID reservationId = this.reservationService.requestReservation(request);
						return ResponseEntity
							.created(uriBuilder.path("/api/reservations/{reservationId}").build(reservationId))
							.build();
					});
	}

	@DeleteMapping(path = "/api/reservations/{reservationId}")
	public ResponseEntity<Void> cancelReservation(@PathVariable UUID reservationId) {
		this.reservationService.cancelReservation(reservationId);
		return ResponseEntity.noContent().build();
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
