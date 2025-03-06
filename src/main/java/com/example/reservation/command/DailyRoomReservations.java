package com.example.reservation.command;

import com.example.event.DomainEvent;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationCancelledEventBuilder;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationConfirmedEventBuilder;
import com.example.event.ReservationEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Aggregate root for all reservations of a specific room on a specific date
 */
public class DailyRoomReservations {

	private final UUID roomId;

	private final LocalDate date;

	private final ConcurrentMap<UUID, Reservation> reservations;

	private DailyRoomReservations(UUID roomId, LocalDate date) {
		this.roomId = roomId;
		this.date = date;
		this.reservations = new ConcurrentHashMap<>();
	}

	public static DailyRoomReservations create(UUID roomId, LocalDate date) {
		return new DailyRoomReservations(roomId, date);
	}

	private boolean hasOverlappingReservation(LocalTime startTime, LocalTime endTime) {
		return reservations.values().stream().anyMatch(r -> {
			if (r.isCancelled()) {
				return false;
			}
			LocalTime existingStart = r.getStartTime();
			LocalTime existingEnd = r.getEndTime();
			// Consider as overlap if start and end times match
			if (existingStart.equals(startTime) && existingEnd.equals(endTime)) {
				return true;
			}
			// Consider as overlap if start and end times intersect or one contains
			// another
			return endTime.isAfter(existingStart) && existingEnd.isAfter(startTime);
		});
	}

	public ReservationConfirmedEvent requestReservation(UUID eventId, UUID reservationId, LocalTime startTime,
			LocalTime endTime, String purpose, UUID userId) {
		if (hasOverlappingReservation(startTime, endTime)) {
			throw new IllegalStateException("Reservation overlap");
		}

		Reservation reservation = ReservationBuilder.reservation()
			.reservationId(reservationId)
			.roomId(roomId)
			.date(date)
			.startTime(startTime)
			.endTime(endTime)
			.purpose(purpose)
			.userId(userId)
			.build();
		reservations.put(reservationId, reservation);

		if (reservation.isCancelled()) {
			throw new IllegalStateException("Reservation is already cancelled");
		}
		return ReservationConfirmedEventBuilder.reservationConfirmedEvent()
			.eventId(eventId)
			.reservationId(reservation.getReservationId())
			.roomId(roomId)
			.userId(reservation.getUserId())
			.date(date)
			.startTime(reservation.getStartTime())
			.endTime(reservation.getEndTime())
			.purpose(reservation.getPurpose())
			.build();
	}

	public ReservationCancelledEvent cancelReservation(UUID eventId, UUID reservationId, UUID cancelUserId) {
		Reservation reservation = reservations.get(reservationId);
		if (reservation == null || reservation.isCancelled()) {
			throw new IllegalArgumentException("Reservation not found or already cancelled");
		}

		reservation.cancel();
		return ReservationCancelledEventBuilder.reservationCancelledEvent()
			.eventId(eventId)
			.reservationId(reservationId)
			.userId(cancelUserId)
			.build();
	}

	public DailyRoomReservations applyEvents(Collection<SortedSet<ReservationEvent>> eventSets) {
		for (SortedSet<ReservationEvent> events : eventSets) {
			if (!events.isEmpty()) {
				Iterator<ReservationEvent> iterator = events.iterator();
				ReservationEvent event1 = iterator.next();
				if (event1 instanceof ReservationConfirmedEvent confirmedEvent) {
					this.applyConfirmedEvent(confirmedEvent);
					if (iterator.hasNext()) {
						DomainEvent event2 = iterator.next();
						if (event2 instanceof ReservationCancelledEvent cancelledEvent) {
							this.applyCancelledEvent(cancelledEvent);
						}
						else {
							throw new IllegalStateException("second event is not a ReservationCancelledEvent");
						}
					}
				}
				else {
					throw new IllegalStateException("first event is not a ReservationConfirmedEvent");
				}
			}
		}
		return this;
	}

	private void applyConfirmedEvent(ReservationConfirmedEvent event) {
		Reservation reservation = ReservationBuilder.reservation()
			.reservationId(event.getReservationId())
			.roomId(event.getRoomId())
			.date(event.getDate())
			.startTime(event.getStartTime())
			.endTime(event.getEndTime())
			.purpose(event.getPurpose())
			.userId(event.getUserId())
			.build();
		reservations.put(event.getReservationId(), reservation);
	}

	private void applyCancelledEvent(ReservationCancelledEvent event) {
		Reservation reservation = reservations.get(event.getReservationId());
		if (reservation != null) {
			reservation.cancel();
		}
	}

}
