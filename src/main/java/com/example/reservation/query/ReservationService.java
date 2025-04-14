package com.example.reservation.query;

import com.example.reservation.command.CancelReservationCommand;
import com.example.reservation.command.RequestReservationCommand;
import com.example.reservation.command.ReservationCommandHandler;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

	public Optional<ReservationView> getReservation(UUID reservationId) {
		return this.reservationViewRepository.findByReservationId(reservationId);
	}

	public List<ReservationView> findByRoomIdAndDate(UUID roomId, LocalDate date) {
		return this.reservationViewRepository.findByRoomIdAndDate(roomId, date);
	}

	public UUID requestReservation(ReservationRequest request) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		UUID reservationId = this.idGenerator.generateId();
		RequestReservationCommand command = new RequestReservationCommand(reservationId, request.roomId(),
				request.date(), request.startTime(), request.endTime(), request.purpose(), userId);
		this.reservationCommandHandler.handleRequestReservation(command);
		return reservationId;
	}

	public void cancelReservation(UUID reservationId) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		CancelReservationCommand command = new CancelReservationCommand(reservationId, userId);
		this.reservationCommandHandler.handleCancelReservation(command);
	}

	public record ReservationRequest(UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
			String purpose) {
	}

}
