package com.example.reservation.command;

import com.example.event.DomainEvent;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

class Reservation {

	private final UUID reservationId;

	private final UUID roomId;

	private final LocalDate date;

	private final LocalTime startTime;

	private final LocalTime endTime;

	private final String purpose;

	private final UUID userId;

	private boolean cancelled;

	private Reservation(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
			String purpose, UUID userId) {
		this.reservationId = reservationId;
		this.roomId = roomId;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.purpose = purpose;
		this.userId = userId;
		this.cancelled = false;
	}

	@Builder(style = BuilderStyle.STAGED)
	public static Reservation create(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime,
			LocalTime endTime, String purpose, UUID userId) {
		return new Reservation(reservationId, roomId, date, startTime, endTime, purpose, userId);
	}

	public static Reservation fromEvents(Collection<ReservationEvent> events) {
		if (events.isEmpty()) {
			throw new IllegalArgumentException("No events provided");
		}
		Iterator<ReservationEvent> iterator = events.iterator();
		ReservationEvent event1 = iterator.next();
		if (event1 instanceof ReservationConfirmedEvent confirmedEvent) {
			Reservation reservation = create(confirmedEvent.getReservationId(), confirmedEvent.getRoomId(),
					confirmedEvent.getDate(), confirmedEvent.getStartTime(), confirmedEvent.getEndTime(),
					confirmedEvent.getPurpose(), confirmedEvent.getUserId());
			if (iterator.hasNext()) {
				DomainEvent event2 = iterator.next();
				if (event2 instanceof ReservationCancelledEvent) {
					reservation.cancel();
				}
				else {
					throw new IllegalArgumentException("second event is not a ReservationCancelledEvent");
				}
			}
			return reservation;
		}
		else {
			throw new IllegalStateException("first event is not a ReservationConfirmedEvent");
		}

	}

	void cancel() {
		this.cancelled = true;
	}

	public UUID getReservationId() {
		return reservationId;
	}

	public UUID getRoomId() {
		return roomId;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getPurpose() {
		return purpose;
	}

	public UUID getUserId() {
		return userId;
	}

	public boolean isCancelled() {
		return cancelled;
	}

}
