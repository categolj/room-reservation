package com.example.reservation.event;

import com.example.TestcontainersConfiguration;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationCancelledEventBuilder;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationConfirmedEventBuilder;
import com.example.reservation.query.ReservationView;
import com.example.reservation.query.ReservationViewRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.IdGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({ TestcontainersConfiguration.class })
@Sql(scripts = { "classpath:delete-test-data.sql", "classpath:insert-test-data.sql" })
class ReservationEventHandlerTest {

	@Autowired
	ReservationEventHandler reservationEventHandler;

	@Autowired
	ReservationViewRepository reservationViewRepository;

	@Autowired
	IdGenerator idGenerator;

	@Test
	void handleReservationConfirmedEvent() {
		UUID eventId = this.idGenerator.generateId();
		UUID reservationId = this.idGenerator.generateId();
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(12, 0);

		ReservationConfirmedEvent event = ReservationConfirmedEventBuilder.reservationConfirmedEvent()
			.eventId(eventId)
			.reservationId(reservationId)
			.roomId(roomId)
			.userId(userId)
			.date(date)
			.startTime(startTime)
			.endTime(endTime)
			.purpose("test")
			.build();

		this.reservationEventHandler.handleReservationEvent(event);

		Awaitility.await().untilAsserted(() -> {
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

	@Test
	void handleReservationCancelledEvent() {
		// まず予約を作成
		UUID eventId = this.idGenerator.generateId();
		UUID reservationId = this.idGenerator.generateId();
		UUID roomId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de3e");
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		LocalDate date = LocalDate.of(2025, 4, 1);
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(12, 0);

		ReservationConfirmedEvent confirmedEvent = ReservationConfirmedEventBuilder.reservationConfirmedEvent()
			.eventId(eventId)
			.reservationId(reservationId)
			.roomId(roomId)
			.userId(userId)
			.date(date)
			.startTime(startTime)
			.endTime(endTime)
			.purpose("test")
			.build();

		this.reservationEventHandler.handleReservationEvent(confirmedEvent);

		Awaitility.await().untilAsserted(() -> {
			// 予約がビューに反映されていることを確認
			assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isPresent();
		});
		// キャンセルイベントを処理
		UUID cancelEventId = this.idGenerator.generateId();
		ReservationCancelledEvent cancelledEvent = ReservationCancelledEventBuilder.reservationCancelledEvent()
			.eventId(cancelEventId)
			.reservationId(reservationId)
			.userId(userId)
			.build();

		this.reservationEventHandler.handleReservationEvent(cancelledEvent);

		Awaitility.await().untilAsserted(() -> {
			// 予約がビューから削除されていることを確認
			assertThat(this.reservationViewRepository.findByReservationId(reservationId)).isEmpty();
		});
	}

}