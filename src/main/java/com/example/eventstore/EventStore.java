package com.example.eventstore;

import com.example.event.DomainEvent;
import com.example.event.DomainEventSerializationView;
import com.example.event.EventType;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventStore {

	private final JdbcClient jdbcClient;

	private final ObjectMapper objectMapper;

	private final ApplicationEventPublisher eventPublisher;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public EventStore(JdbcClient jdbcClient, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
		this.jdbcClient = jdbcClient;
		this.objectMapper = objectMapper;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public void store(DomainEvent event) {
		logger.info("Storing event {}", event);
		EventStoreEntry entry = EventStoreEntryBuilder.eventStoreEntry()
			.eventId(event.getEventId())
			.eventType(event.getEventType())
			.payload(serialize(event))
			.build();

		this.jdbcClient
			.sql("""
					INSERT INTO event_store (event_id, event_type, payload) VALUES (:event_id, :event_type::EVENT_TYPE, :payload::JSONB)
					"""
				.trim())
			.param("event_id", entry.eventId())
			.param("event_type", entry.eventType().name())
			.param("payload", entry.payload())
			.update();

		// Publish event after successful storage
		this.eventPublisher.publishEvent(event);
	}

	private String serialize(DomainEvent event) {
		try {
			return objectMapper.writerWithView(DomainEventSerializationView.class).writeValueAsString(event);
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	private DomainEvent deserialize(EventStoreEntry entry) {
		try {
			EventType eventType = entry.eventType();
			// Completing payload fields excluded by DomainEventSerializationView during
			// serialization
			String payload = entry.payload().replaceFirst("\\{", """
					{"eventId": "%s", "eventType": "%s",
					""".formatted(entry.eventId(), eventType));
			if (eventType == EventType.RESERVATION_CONFIRMED) {
				return objectMapper.readValue(payload, ReservationConfirmedEvent.class);
			}
			else if (eventType == EventType.RESERVATION_CANCELLED) {
				return objectMapper.readValue(payload, ReservationCancelledEvent.class);
			}
			else {
				return objectMapper.readValue(payload, DomainEvent.class);
			}
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public List<ReservationEvent> findReservationEventsByReservationId(UUID reservationId) {
		List<EventStoreEntry> entries = this.jdbcClient.sql("""
				SELECT event_id, event_type, payload
				FROM event_store
				WHERE event_type IN ('RESERVATION_CONFIRMED', 'RESERVATION_CANCELLED')
				AND payload ->> 'reservationId' = :reservation_id
				ORDER BY event_id
				""".trim()).param("reservation_id", reservationId.toString()).query(EventStoreEntry.class).list();
		return entries.stream()
			.map(this::deserialize)
			.filter(event -> event instanceof ReservationEvent)
			.map(event -> (ReservationEvent) event)
			.toList();
	}

	public Collection<SortedSet<ReservationEvent>> findReservationEventsByRoomIdAndDate(UUID roomId, LocalDate date) {
		List<EventStoreEntry> events = this.jdbcClient.sql("""
				WITH active_reservations AS (
					SELECT DISTINCT payload ->> 'reservationId' as reservation_id
					FROM event_store
					WHERE event_type = 'RESERVATION_CONFIRMED'
					AND payload ->> 'roomId' = :room_id
					AND payload ->> 'date' = :date
					EXCEPT
					SELECT payload ->> 'reservationId'
					FROM event_store
					WHERE event_type = 'RESERVATION_CANCELLED'
				)
				SELECT e.event_id, e.event_type, e.payload
				FROM event_store e
				JOIN active_reservations ar ON e.payload ->> 'reservationId' = ar.reservation_id
				ORDER BY e.event_id
				""".trim())
			.param("room_id", roomId.toString())
			.param("date", date.toString())
			.query(EventStoreEntry.class)
			.list();
		// Group events by reservationId
		Map<UUID, SortedSet<ReservationEvent>> reservationEvents = events.stream()
			.map(this::deserialize)
			.filter(event -> event instanceof ReservationEvent)
			.map(event -> (ReservationEvent) event)
			.collect(Collectors.groupingBy(ReservationEvent::getReservationId,
					Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ReservationEvent::getEventId)))));
		return reservationEvents.values();
	}

}
