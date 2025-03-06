package com.example.event;

import com.fasterxml.jackson.annotation.JsonView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

public final class ReservationConfirmedEvent extends ReservationEvent {

	private final UUID roomId;

	private final LocalDate date;

	private final LocalTime startTime;

	private final LocalTime endTime;

	private final String purpose;

	@Builder(style = BuilderStyle.STAGED)
	ReservationConfirmedEvent(UUID eventId, UUID reservationId, UUID roomId, UUID userId, LocalDate date,
			LocalTime startTime, LocalTime endTime, String purpose) {
		super(eventId, EventType.RESERVATION_CONFIRMED, reservationId, userId);
		this.roomId = roomId;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.purpose = purpose;
	}

	@JsonView(DomainEventSerializationView.class)
	public UUID getRoomId() {
		return roomId;
	}

	@JsonView(DomainEventSerializationView.class)
	public LocalDate getDate() {
		return date;
	}

	@JsonView(DomainEventSerializationView.class)
	public LocalTime getStartTime() {
		return startTime;
	}

	@JsonView(DomainEventSerializationView.class)
	public LocalTime getEndTime() {
		return endTime;
	}

	@JsonView(DomainEventSerializationView.class)
	public String getPurpose() {
		return purpose;
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
