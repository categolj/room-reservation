package com.example.availability.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class AvailabilityViewRepository {

	private final JdbcClient jdbcClient;

	public AvailabilityViewRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Transactional
	public Optional<AvailabilityView> findOneForUpdate(UUID roomId, LocalDate date) {
		return this.jdbcClient.sql("""
				SELECT ra.room_id, r.room_name, ra.date FROM room_availability_view AS ra
				JOIN room_view AS r ON ra.room_id = r.room_id
				WHERE ra.room_id = :roomId AND date = :date
				LIMIT 1
				FOR UPDATE
				""".trim()).param("roomId", roomId).param("date", date).query(AvailabilityView.class).optional();
	}

	public List<AvailabilityView> findByDate(LocalDate date) {
		return this.jdbcClient.sql("""
				SELECT ra.room_id, r.room_name, ra.date FROM room_availability_view AS ra
				JOIN room_view AS r ON ra.room_id = r.room_id
				WHERE date = :date
				ORDER BY ra.room_id
				""".trim()).param("date", date).query(AvailabilityView.class).list();
	}

}
