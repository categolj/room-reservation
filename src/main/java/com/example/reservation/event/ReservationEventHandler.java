package com.example.reservation.event;

import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventHandler {

	private final JdbcClient jdbcClient;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public ReservationEventHandler(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@ApplicationModuleListener
	public void handleReservationEvent(ReservationEvent event) {
		logger.info("handleReservationEvent: {}", event);
		switch (event) {
			case ReservationConfirmedEvent confirmedEvent -> this.handleReservationConfirmedEvent(confirmedEvent);
			case ReservationCancelledEvent cancelledEvent -> this.handleReservationCancelledEvent(cancelledEvent);
		}
	}

	private void handleReservationConfirmedEvent(ReservationConfirmedEvent confirmedEvent) {
		this.jdbcClient.sql("""
				INSERT INTO reservation_view (
				    reservation_id,
				    room_id,
				    date,
				    start_time,
				    end_time,
				    purpose,
				    user_id
				) VALUES (
				    :reservationId,
				    :roomId,
				    :date,
				    :startTime,
				    :endTime,
				    :purpose,
				    :userId
				)
				ON CONFLICT (reservation_id) DO UPDATE SET
				    room_id = EXCLUDED.room_id,
				    date = EXCLUDED.date,
				    start_time = EXCLUDED.start_time,
				    end_time = EXCLUDED.end_time,
				    purpose = EXCLUDED.purpose,
				    user_id = EXCLUDED.user_id
				""".trim())
			.param("reservationId", confirmedEvent.getReservationId())
			.param("roomId", confirmedEvent.getRoomId())
			.param("date", confirmedEvent.getDate())
			.param("startTime", confirmedEvent.getStartTime())
			.param("endTime", confirmedEvent.getEndTime())
			.param("purpose", confirmedEvent.getPurpose())
			.param("userId", confirmedEvent.getUserId())
			.update();
	}

	private void handleReservationCancelledEvent(ReservationCancelledEvent cancelledEvent) {
		this.jdbcClient.sql("""
				DELETE FROM reservation_view
				WHERE reservation_id = :reservationId
				""".trim()).param("reservationId", cancelledEvent.getReservationId()).update();
	}

}
