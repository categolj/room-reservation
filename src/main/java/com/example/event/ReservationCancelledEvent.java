package com.example.event;

import java.util.UUID;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

public final class ReservationCancelledEvent extends ReservationEvent {

	@Builder(style = BuilderStyle.STAGED)
	public ReservationCancelledEvent(UUID eventId, UUID reservationId, UUID userId) {
		super(eventId, EventType.RESERVATION_CANCELLED, reservationId, userId);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
