package com.example.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public abstract class DomainEvent {

	private final UUID eventId;

	private final EventType eventType;

	protected DomainEvent(UUID eventId, EventType eventType) {
		Assert.isTrue(eventId != null, "eventId must not be null");
		Assert.isTrue(eventType != null, "eventType must not be null");
		this.eventId = eventId;
		this.eventType = eventType;
	}

	public UUID getEventId() {
		return eventId;
	}

	public EventType getEventType() {
		return eventType;
	}

	@Nullable
	public Instant getCreatedAt() {
		if (this.eventId.version() != 7) {
			return null;
		}
		long epochMilli = this.eventId.getMostSignificantBits() >> 16;
		return Instant.ofEpochMilli(epochMilli);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		DomainEvent that = (DomainEvent) o;
		return Objects.equals(eventId, that.eventId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(eventId);
	}

	@Override
	public String toString() {
		return "DomainEvent{" + "eventId=" + eventId + ", eventType=" + eventType + ", createdAt=" + getCreatedAt()
				+ '}';
	}

}
