package com.example.reservation.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Read-only repository for reservation information
 */
@Repository
public class ReservationViewRepository {

	private final JdbcClient jdbcClient;

	public ReservationViewRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Optional<ReservationView> findByReservationId(UUID reservationId) {
		return this.jdbcClient.sql("""
				SELECT reservation_id,
				    room_id,
				    date,
				    start_time,
				    end_time,
				    purpose,
				    user_id
				FROM reservation_view
				WHERE reservation_id = :reservationId
				""".trim()).param("reservationId", reservationId).query(ReservationView.class).optional();
	}

	public List<ReservationView> findByRoomIdAndDate(UUID roomId, LocalDate date) {
		return this.jdbcClient.sql("""
				SELECT reservation_id,
				    room_id,
				    date,
				    start_time,
				    end_time,
				    purpose,
				    user_id
				FROM reservation_view
				WHERE room_id = :roomId AND date = :date
				""".trim()).param("roomId", roomId).param("date", date).query(ReservationView.class).list();
	}

}
