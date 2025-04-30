package com.example.reservation.query;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments5;
import am.ik.yavi.arguments.Arguments5Validator;
import com.example.reservation.command.CancelReservationCommand;
import com.example.reservation.command.RequestReservationCommand;
import com.example.reservation.command.ReservationCommandHandler;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;
import org.springframework.util.StringUtils;

import static am.ik.yavi.arguments.ArgumentsValidators.split;
import static com.example.reservation.command.Reservation.dateValidator;
import static com.example.reservation.command.Reservation.purposeValidator;
import static com.example.reservation.command.Reservation.roomIdValidator;
import static com.example.reservation.command.Reservation.starTimeAndEndTimeValidator;

@Service
public class ReservationService {

	private final ReservationCommandHandler reservationCommandHandler;

	private final ReservationViewRepository reservationViewRepository;

	private final IdGenerator idGenerator;

	public ReservationService(ReservationCommandHandler reservationCommandHandler,
			ReservationViewRepository reservationViewRepository, IdGenerator idGenerator) {
		this.reservationCommandHandler = reservationCommandHandler;
		this.reservationViewRepository = reservationViewRepository;
		this.idGenerator = idGenerator;
	}

	@Tool(description = """
			Retrieves a reservation by its unique identifier.
			Returns an Optional containing the reservation view if found, or an empty Optional if the reservation does not exist.
			Don't forget that the reservationId param is a UUID.
			""")
	public Optional<ReservationView> getReservation(
			@ToolParam(description = "The unique identifier of the reservation to retrieve.") UUID reservationId) {
		return this.reservationViewRepository.findByReservationId(reservationId);
	}

	@Tool(description = """
			Finds all reservations for a specific room on a given date.
			Returns a list of reservation views matching the room ID and date criteria.
			Don't forget that the roomId param is a UUID and the date param is a date in yyyy-mm-dd format.
			The output should include reservationId so that the UUID can be inherited.
			""")
	public List<ReservationView> findByRoomIdAndDate(
			@ToolParam(description = "The unique identifier of the room to find reservations for.") UUID roomId,
			@ToolParam(description = "The date on which to find reservations.") LocalDate date) {
		return this.reservationViewRepository.findByRoomIdAndDate(roomId, date);
	}

	@Tool(description = """
			Creates a new reservation request based on the provided details.
			Returns the newly generated reservation ID.
			Don't forget that the reservationId param is a UUID, the date param is a date in yyyy-mm-dd format, and startTime and endTime are times in HH:mm:ss format.
			""")
	public UUID requestReservation(@ToolParam(
			description = "A record containing all necessary information for the reservation including room ID, date, start time, end time, and purpose.") ReservationRequest request) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		UUID reservationId = this.idGenerator.generateId();
		RequestReservationCommand command = new RequestReservationCommand(reservationId, request.roomId(),
				request.date(), request.startTime(), request.endTime(), request.purpose(), userId);
		this.reservationCommandHandler.handleRequestReservation(command);
		return reservationId;
	}

	@Tool(description = """
			Cancels an existing reservation based on its unique identifier.
			Don't forget that the reservationId param is a UUID.
			""")
	public void cancelReservation(
			@ToolParam(description = "The unique identifier of the reservation to cancel.") UUID reservationId) {
		UUID userId = UUID.fromString("018422b2-4843-7a62-935b-b4e65649de46");
		CancelReservationCommand command = new CancelReservationCommand(reservationId, userId);
		this.reservationCommandHandler.handleCancelReservation(command);
	}

	public record ReservationRequest(UUID roomId, LocalDate date,
			@JsonDeserialize(using = FlexibleLocalTimeDeserializer.class) LocalTime startTime,
			@JsonDeserialize(using = FlexibleLocalTimeDeserializer.class) LocalTime endTime, String purpose) {

		public static final Arguments5Validator<UUID, LocalDate, LocalTime, LocalTime, String, ReservationRequest> validator = Arguments5Validator
			.unwrap(split(roomIdValidator, dateValidator).apply(Arguments::of)
				.<Arguments5<UUID, LocalDate, LocalTime, LocalTime, String>>compose(Arguments5::first2)
				.combine(starTimeAndEndTimeValidator.compose(args -> Arguments.of(args.arg3(), args.arg4())))
				.combine(purposeValidator.wrap().compose(args -> Arguments.of(args.arg5())))
				.apply((a1, a2, a3) -> new ReservationRequest(Objects.requireNonNull(a1).arg1(), a1.arg2(),
						Objects.requireNonNull(a2).arg1(), a2.arg2(), a3)));

		public ReservationRequest {
			validator.lazy().validated(roomId, date, startTime, endTime, purpose);
		}

		/**
		 * Custom deserializer for LocalTime that accepts more flexible input formats.
		 * Extracts just the HH:mm:ss part from the time string, ignoring any timezone
		 * information or other suffixes. This is particularly useful for processing
		 * inputs from LLMs which might include additional time information not compatible
		 * with LocalTime.
		 */
		public static class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {

			// Pattern to extract just the time part (HH:mm:ss)
			private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}).*");

			@Override
			public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				String value = p.getValueAsString();
				if (!StringUtils.hasText(value)) {
					return null;
				}

				// Extract just the HH:mm:ss part from the string
				Matcher matcher = TIME_PATTERN.matcher(value);
				if (matcher.matches()) {
					value = matcher.group(1);
				}

				// Parse the extracted time string
				return LocalTime.parse(value);
			}

		}
	}

}
