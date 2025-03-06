package com.example.event;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.UUID;

public abstract sealed class ReservationEvent extends DomainEvent
		permits ReservationConfirmedEvent, ReservationCancelledEvent {

	protected final UUID reservationId;

	protected final UUID userId;

	protected ReservationEvent(UUID eventId, EventType eventType, UUID reservationId, UUID userId) {
		super(eventId, eventType);
		this.reservationId = reservationId;
		this.userId = userId;
	}

	@JsonView(DomainEventSerializationView.class)
	public UUID getReservationId() {
		return reservationId;
	}

	@JsonView(DomainEventSerializationView.class)
	public UUID getUserId() {
		return userId;
	}

	@Override
	public String toString() {
		return "ReservationEvent{" + "eventId=" + getEventId() + ", eventType=" + getEventType() + ", createdAt="
				+ getCreatedAt() + ", userId=" + userId + ", reservationId=" + reservationId + '}';
	}

}
