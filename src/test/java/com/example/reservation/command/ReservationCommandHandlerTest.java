package com.example.reservation.command;

import com.example.TestcontainersConfiguration;
import com.example.reservation.query.ReservationView;
import com.example.reservation.query.ReservationViewRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.IdGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = { "logging.level.org.springframework.jdbc.support.JdbcTransactionManager=DEBUG" })
@Import({ TestcontainersConfiguration.class })
@Sql(scripts = { "classpath:delete-test-data.sql", "classpath:insert-test-data.sql" })
class ReservationCommandHandlerTest {

	@Autowired
	ReservationCommandHandler reservationCommandHandler;

	@Autowired
	ReservationViewRepository reservationViewRepository;

	@Autowired
	IdGenerator idGenerator;

	@Test
	void handleRequestReservation() {
		UUID reservationId = idGenerator.generateId();
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(12, 0);
		RequestReservationCommand command = new RequestReservationCommand(reservationId, roomId, date, startTime,
				endTime, "test", userId);
		this.reservationCommandHandler.handleRequestReservation(command);
		await().untilAsserted(() -> {
			Optional<ReservationView> view = this.reservationViewRepository.findByReservationId(reservationId);
			assertThat(view).isPresent();
			ReservationView reservationView = view.get();
			assertThat(reservationView.roomId()).isEqualTo(roomId);
			assertThat(reservationView.date()).isEqualTo(date);
			assertThat(reservationView.startTime()).isEqualTo(startTime);
			assertThat(reservationView.endTime()).isEqualTo(endTime);
			assertThat(reservationView.purpose()).isEqualTo("test");
		});
	}

	//
	@Test
	void handleRequestReservation_overlapped() {
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		{
			LocalTime startTime = LocalTime.of(10, 0);
			LocalTime endTime = LocalTime.of(12, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand command = new RequestReservationCommand(reservationId, roomId, date, startTime,
					endTime, "test1", userId);
			this.reservationCommandHandler.handleRequestReservation(command);
			await().untilAsserted(() -> {
				Optional<ReservationView> view = this.reservationViewRepository.findByReservationId(reservationId);
				assertThat(view).isPresent();
				ReservationView reservationView = view.get();
				assertThat(reservationView.roomId()).isEqualTo(roomId);
				assertThat(reservationView.date()).isEqualTo(date);
				assertThat(reservationView.startTime()).isEqualTo(startTime);
				assertThat(reservationView.endTime()).isEqualTo(endTime);
				assertThat(reservationView.purpose()).isEqualTo("test1");
			});
		}
		{
			LocalTime startTime = LocalTime.of(9, 0);
			LocalTime endTime = LocalTime.of(11, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand command = new RequestReservationCommand(reservationId, roomId, date, startTime,
					endTime, "test2", userId);
			assertThatThrownBy(() -> this.reservationCommandHandler.handleRequestReservation(command))
				.hasMessageContaining("Reservation overlap");
		}
	}

	//
	@Test
	void handleRequestReservation_notOverlapped() {
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		{
			LocalTime startTime = LocalTime.of(10, 0);
			LocalTime endTime = LocalTime.of(12, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand command = new RequestReservationCommand(reservationId, roomId, date, startTime,
					endTime, "test1", userId);
			this.reservationCommandHandler.handleRequestReservation(command);
		}
		{
			LocalTime startTime = LocalTime.of(12, 0);
			LocalTime endTime = LocalTime.of(13, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand command = new RequestReservationCommand(reservationId, roomId, date, startTime,
					endTime, "test2", userId);
			this.reservationCommandHandler.handleRequestReservation(command);
		}
	}

	//
	@Test
	void handleCancelReservation() {
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(12, 0);

		UUID reservationId = idGenerator.generateId();
		RequestReservationCommand requestCommand = new RequestReservationCommand(reservationId, roomId, date, startTime,
				endTime, "test", userId);
		this.reservationCommandHandler.handleRequestReservation(requestCommand);

		await().untilAsserted(
				() -> assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isPresent());

		CancelReservationCommand cancelCommand = new CancelReservationCommand(reservationId, userId);
		this.reservationCommandHandler.handleCancelReservation(cancelCommand);

		await().untilAsserted(
				() -> assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isEmpty());
	}

	@Test
	void handleCancelReservation_notFound() {
		UUID reservationId = this.idGenerator.generateId();
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		CancelReservationCommand command = new CancelReservationCommand(reservationId, userId);
		assertThatThrownBy(() -> this.reservationCommandHandler.handleCancelReservation(command))
			.hasMessageContaining("Reservation not found");
	}

	@Test
	void handleRequestReservation_after_cancel() {
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		{
			LocalTime startTime = LocalTime.of(10, 0);
			LocalTime endTime = LocalTime.of(12, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand requestCommand = new RequestReservationCommand(reservationId, roomId, date,
					startTime, endTime, "test1", userId);
			this.reservationCommandHandler.handleRequestReservation(requestCommand);
			await().untilAsserted(
					() -> assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isPresent());
			CancelReservationCommand cancelCommand = new CancelReservationCommand(reservationId, userId);
			this.reservationCommandHandler.handleCancelReservation(cancelCommand);
			await().untilAsserted(
					() -> assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isEmpty());
		}
		{
			LocalTime startTime = LocalTime.of(9, 0);
			LocalTime endTime = LocalTime.of(11, 0);
			UUID reservationId = idGenerator.generateId();
			RequestReservationCommand requestCommand = new RequestReservationCommand(reservationId, roomId, date,
					startTime, endTime, "test2", userId);
			this.reservationCommandHandler.handleRequestReservation(requestCommand);
			await().untilAsserted(
					() -> assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isPresent());
		}
	}

}
