package com.example.reservation.command;

import com.example.availability.query.AvailabilityViewRepository;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationEvent;
import com.example.eventstore.EventStore;
import com.example.user.query.UserViewRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

@Service
public class ReservationCommandHandler {

	private final EventStore eventStore;

	private final AvailabilityViewRepository availabilityViewRepository;

	private final UserViewRepository userViewRepository;

	private final IdGenerator idGenerator;

	public ReservationCommandHandler(EventStore eventStore, AvailabilityViewRepository availabilityViewRepository,
			UserViewRepository userViewRepository, IdGenerator idGenerator) {
		this.eventStore = eventStore;
		this.availabilityViewRepository = availabilityViewRepository;
		this.userViewRepository = userViewRepository;
		this.idGenerator = idGenerator;
	}

	@Transactional
	public void handleRequestReservation(RequestReservationCommand command) {
		this.availabilityViewRepository.findOneForUpdate(command.roomId(), command.date())
			.orElseThrow(() -> new IllegalArgumentException("The room is not available for the date."));
		this.userViewRepository.findByUserId(command.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
		UUID eventId = this.idGenerator.generateId();

		// Get or create DailyRoomReservations
		DailyRoomReservations dailyReservations = findOrCreateDailyRoomReservations(command.roomId(), command.date());

		// Request reservation
		ReservationConfirmedEvent confirmedEvent = dailyReservations.requestReservation(eventId,
				command.reservationId(), command.startTime(), command.endTime(), command.purpose(), command.userId());

		// Store event
		this.eventStore.store(confirmedEvent);
	}

	@Transactional
	public void handleCancelReservation(CancelReservationCommand command) {
		// First find the reservation events
		List<ReservationEvent> events = this.eventStore.findReservationEventsByReservationId(command.reservationId());
		if (events.isEmpty()) {
			throw new IllegalArgumentException("Reservation not found");
		}

		// Rebuild reservation from events
		Reservation reservation = Reservation.fromEvents(events);

		// Get the DailyRoomReservations for the reservation's room and date
		DailyRoomReservations dailyReservations = findOrCreateDailyRoomReservations(reservation.getRoomId(),
				reservation.getDate());

		UUID eventId = this.idGenerator.generateId();
		ReservationCancelledEvent cancelledEvent = dailyReservations.cancelReservation(eventId, command.reservationId(),
				command.userId());

		this.eventStore.store(cancelledEvent);
	}

	private DailyRoomReservations findOrCreateDailyRoomReservations(UUID roomId, LocalDate date) {
		Collection<SortedSet<ReservationEvent>> eventSets = this.eventStore.findReservationEventsByRoomIdAndDate(roomId,
				date);
		return DailyRoomReservations.create(roomId, date).applyEvents(eventSets);
	}

}
