package com.example.reservation.web;

import com.example.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ TestcontainersConfiguration.class })
@Sql(scripts = { "classpath:delete-test-data.sql", "classpath:insert-test-data.sql" })
class ReservationControllerTest {

	RestClient restClient;

	LocalDate today = LocalDate.now();

	@BeforeEach
	void setUp(@Autowired RestClient.Builder restClientBuilder, @LocalServerPort int port) {
		this.restClient = restClientBuilder.baseUrl("http://localhost:" + port)
			.defaultStatusHandler(__ -> true, ((request, response) -> {
			}))
			.build();
	}

	@Test
	void getReservation() {
		ResponseEntity<JsonNode> response = this.restClient.get()
			.uri("/api/reservations/018422b2-4843-7a62-935b-b4e65649de4b")
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode responseBody = response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.get("reservationId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de4b");
		assertThat(responseBody.get("roomId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de3e");
		assertThat(responseBody.get("userId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de46");
		assertThat(responseBody.get("date").asText()).isEqualTo("2025-03-04");
		assertThat(responseBody.get("startTime").asText()).isEqualTo("09:00:00");
		assertThat(responseBody.get("endTime").asText()).isEqualTo("10:30:00");
		assertThat(responseBody.get("purpose").asText()).isEqualTo("Weekly Project Meeting");
	}

	@Test
	void findReservation() {
		ResponseEntity<JsonNode[]> response = this.restClient.get()
			.uri("/api/reservations?roomId=018422b2-4843-7a62-935b-b4e65649de3e&date=2025-03-04")
			.retrieve()
			.toEntity(JsonNode[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode[] responseBody = response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody).hasSize(2);

		// First reservation
		assertThat(responseBody[0].get("reservationId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de4b");
		assertThat(responseBody[0].get("roomId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de3e");
		assertThat(responseBody[0].get("userId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de46");
		assertThat(responseBody[0].get("date").asText()).isEqualTo("2025-03-04");
		assertThat(responseBody[0].get("startTime").asText()).isEqualTo("09:00:00");
		assertThat(responseBody[0].get("endTime").asText()).isEqualTo("10:30:00");
		assertThat(responseBody[0].get("purpose").asText()).isEqualTo("Weekly Project Meeting");

		// Second reservation
		assertThat(responseBody[1].get("reservationId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de50");
		assertThat(responseBody[1].get("roomId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de3e");
		assertThat(responseBody[1].get("userId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de4a");
		assertThat(responseBody[1].get("date").asText()).isEqualTo("2025-03-04");
		assertThat(responseBody[1].get("startTime").asText()).isEqualTo("11:00:00");
		assertThat(responseBody[1].get("endTime").asText()).isEqualTo("11:30:00");
		assertThat(responseBody[1].get("purpose").asText()).isEqualTo("1-on-1 Meeting");
	}

	@Test
	void requestReservation() {
		var request = """
				{
					"roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
					"date": "%s",
					"startTime": "13:00:00",
					"endTime": "14:00:00",
				 "purpose": "New Reservation Test"
				}
				""".formatted(today);
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/api/reservations")
			.header("Content-Type", "application/json")
			.body(request)
			.retrieve()
			.toBodilessEntity();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		String locationPath = response.getHeaders().getLocation().getPath();
		assertThat(locationPath).startsWith("/api/reservations/");

		Awaitility.await().untilAsserted(() -> {
			// Verify the created reservation
			ResponseEntity<JsonNode> getResponse = this.restClient.get()
				.uri(locationPath)
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			JsonNode reservation = getResponse.getBody();
			assertThat(reservation).isNotNull();
			assertThat(reservation.get("roomId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de3e");
			assertThat(reservation.get("userId").asText()).isEqualTo("018422b2-4843-7a62-935b-b4e65649de46");
			assertThat(reservation.get("date").asText()).isEqualTo(today.toString());
			assertThat(reservation.get("startTime").asText()).isEqualTo("13:00:00");
			assertThat(reservation.get("endTime").asText()).isEqualTo("14:00:00");
			assertThat(reservation.get("purpose").asText()).isEqualTo("New Reservation Test");
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	void requestReservationInvalidRequest() {
		var request = """
				{
					"roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
					"startTime": "15:00:00",
					"endTime": "14:00:00",
					"purpose": ""
				}
				""";
		ResponseEntity<ProblemDetail> response = this.restClient.post()
			.uri("/api/reservations")
			.header("Content-Type", "application/json")
			.body(request)
			.retrieve()
			.toEntity(ProblemDetail.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		ProblemDetail problemDetail = response.getBody();
		assertThat(problemDetail).isNotNull();
		assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
		assertThat(problemDetail.getDetail()).isEqualTo("Constraint violations found!");
		Map<String, Object> properties = problemDetail.getProperties();
		assertThat(properties).isNotNull();
		assertThat(properties).containsKey("violations");
		List<Map<String, Object>> violations = (List<Map<String, Object>>) properties.get("violations");
		assertThat(violations).containsExactlyInAnyOrderElementsOf(
				List.of(Map.of("message", "\"date\" must not be null", "field", "date"),
						Map.of("message", "\"endTime\" must be later than \"startTime\"", "field", "endTime"),
						Map.of("message",
								"The byte size of \"purpose\" must be greater than or equal to 1. The given size is 0",
								"field", "purpose")));
	}

	@Test
	void cancelReservation() {
		UUID reservationId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de4b");

		// First verify the reservation exists
		ResponseEntity<JsonNode> getResponse = this.restClient.get()
			.uri("/api/reservations/" + reservationId)
			.retrieve()
			.toEntity(JsonNode.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Cancel the reservation
		ResponseEntity<Void> response = this.restClient.delete()
			.uri("/api/reservations/" + reservationId)
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		Awaitility.await().untilAsserted(() -> {
			// Verify the reservation no longer exists
			ResponseEntity<JsonNode> getAfterCancelResponse = this.restClient.get()
				.uri("/api/reservations/" + reservationId)
				.retrieve()
				.toEntity(JsonNode.class);
			assertThat(getAfterCancelResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		});
	}

	@Test
	void requestReservationWhenOverlapping() {
		{
			var request = """
					{
						"roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
						"date": "%s",
						"startTime": "10:00:00",
						"endTime": "11:00:00",
					 "purpose": "New Reservation Test"
					}
					""".formatted(today);
			ResponseEntity<Void> response = this.restClient.post()
				.uri("/api/reservations")
				.header("Content-Type", "application/json")
				.body(request)
				.retrieve()
				.toBodilessEntity();
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		}
		{
			var request = """
					{
						"roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
						"date": "%s",
						"startTime": "09:00:00",
						"endTime": "10:30:00",
						"purpose": "Overlapping Reservation Test"
					}
					""".formatted(today);
			ResponseEntity<ErrorResponse> response = this.restClient.post()
				.uri("/api/reservations")
				.header("Content-Type", "application/json")
				.body(request)
				.retrieve()
				.toEntity(ErrorResponse.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().message()).isEqualTo("Reservation overlap");
		}
	}

	@Test
	void requestReservationWhenNotOverlapping() {
		var request = """
				{
					"roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
					"date": "%s",
					"startTime": "10:30:00",
					"endTime": "11:00:00",
					"purpose": "Non-overlapping Reservation Test"
				}
				""".formatted(today);
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/api/reservations")
			.header("Content-Type", "application/json")
			.body(request)
			.retrieve()
			.toBodilessEntity();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
	}

	@Test
	void requestReservationConcurrently() throws Exception {
		int numberOfThreads = 5;
		CountDownLatch startLatch = new CountDownLatch(1);
		List<Future<ResponseEntity<?>>> futures;
		var request = """
				{
				  "roomId": "018422b2-4843-7a62-935b-b4e65649de3e",
				  "date": "%s",
				  "startTime": "15:00:00",
				  "endTime": "16:00:00",
				  "purpose": "Concurrent Reservation Test"
				}
				""".formatted(today);
		try (ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads)) {
			futures = new ArrayList<>();
			// Submit concurrent requests
			for (int i = 0; i < numberOfThreads; i++) {
				futures.add(executorService.submit(() -> {
					try {
						startLatch.await(); // Wait for all threads to be ready
						return this.restClient.post()
							.uri("/api/reservations")
							.header("Content-Type", "application/json")
							.body(request)
							.retrieve()
							.toBodilessEntity();
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(e);
					}
				}));
			}

			// Start all threads simultaneously
			startLatch.countDown();
		}

		// Count successful and failed requests
		int successCount = 0;
		int conflictCount = 0;

		for (Future<ResponseEntity<?>> future : futures) {
			ResponseEntity<?> response = future.get();
			if (response.getStatusCode() == HttpStatus.CREATED) {
				successCount++;
			}
			else if (response.getStatusCode() == HttpStatus.CONFLICT) {
				conflictCount++;
			}
		}

		// Verify that exactly one request succeeded and others failed
		assertThat(successCount).isEqualTo(1);
		assertThat(conflictCount).isEqualTo(numberOfThreads - 1);
	}

	@Test
	void cancelAlreadyCancelledReservation() {
		UUID reservationId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de4b");

		// First cancel
		ResponseEntity<Void> firstCancelResponse = this.restClient.delete()
			.uri("/api/reservations/" + reservationId)
			.retrieve()
			.toBodilessEntity();
		assertThat(firstCancelResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// Second cancel should fail
		ResponseEntity<ErrorResponse> secondCancelResponse = this.restClient.delete()
			.uri("/api/reservations/" + reservationId)
			.retrieve()
			.toEntity(ErrorResponse.class);
		assertThat(secondCancelResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(secondCancelResponse.getBody()).isNotNull();
		assertThat(secondCancelResponse.getBody().message()).isEqualTo("Reservation not found or already cancelled");
	}

}
