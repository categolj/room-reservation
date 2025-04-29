package com.example.reservation.command;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.Arguments7;
import am.ik.yavi.arguments.Arguments7Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.LocalDateValidator;
import am.ik.yavi.arguments.LocalTimeValidator;
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.LocalDateValidatorBuilder;
import am.ik.yavi.builder.LocalTimeValidatorBuilder;
import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.Constraint;
import com.example.event.DomainEvent;
import com.example.event.ReservationCancelledEvent;
import com.example.event.ReservationConfirmedEvent;
import com.example.event.ReservationEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.jilt.Builder;
import org.jilt.BuilderStyle;

public class Reservation {

	private final UUID reservationId;

	private final UUID roomId;

	private final LocalDate date;

	private final LocalTime startTime;

	private final LocalTime endTime;

	private final String purpose;

	private final UUID userId;

	private boolean cancelled;

	public static final ObjectValidator<UUID, UUID> reservationIdValidator = ObjectValidatorBuilder
		.<UUID>of("reservationId", Constraint::notNull)
		.build();

	public static final ObjectValidator<UUID, UUID> roomIdValidator = ObjectValidatorBuilder
		.<UUID>of("roomId", Constraint::notNull)
		.build();

	public static final LocalDateValidator<LocalDate> dateValidator = LocalDateValidatorBuilder
		.of("date", c -> c.notNull())
		.build();

	private static final Function<String, LocalTimeValidator<LocalTime>> timeValidator = name -> LocalTimeValidatorBuilder
		.of(name,
				c -> c.notNull()
					.predicate(value -> value == null || value.getMinute() % 30 == 0, "time.thirtyminutes",
							"Enter \"{0}\" in 30-minute increments"))
		.build();

	public static final Arguments2Validator<LocalTime, LocalTime, Arguments2<LocalTime, LocalTime>> starTimeAndEndTimeValidator = ArgumentsValidators
		.split(timeValidator.apply("startTime"), timeValidator.apply("endTime"))
		.apply(Arguments::of)
		.andThen(ObjectValidatorBuilder
			.<Arguments2<LocalTime, LocalTime>>of("endTime",
					c -> c.predicate(args -> Objects.requireNonNull(args.arg2()).isAfter(args.arg1()),
							"time.endtimemustbelaterthanstarttime", "\"endTime\" must be later than \"startTime\""))
			.build());

	public static StringValidator<String> purposeValidator = StringValidatorBuilder
		.of("purpose", c -> c.notNull().asByteArray().greaterThanOrEqual(1).lessThanOrEqual(255))
		.build();

	public static final ObjectValidator<UUID, UUID> userIdValidator = ObjectValidatorBuilder
		.<UUID>of("userId", Constraint::notNull)
		.build();

	public static final Arguments7Validator<UUID, UUID, LocalDate, LocalTime, LocalTime, String, UUID, Reservation> reservationValidator = Arguments7Validator
		.unwrap(reservationIdValidator.split(roomIdValidator)
			.split(dateValidator)
			.apply(Arguments::of)
			.<Arguments7<UUID, UUID, LocalDate, LocalTime, LocalTime, String, UUID>>compose(Arguments7::first3)
			.combine(starTimeAndEndTimeValidator.compose(args -> Arguments.of(args.arg4(), args.arg5())))
			.combine(purposeValidator.split(userIdValidator).apply(Arguments::of).compose(Arguments7::last2))
			.apply((a1, a2, a3) -> new Reservation(a1.arg1(), a1.arg2(), a1.arg3(), a2.arg1(), a2.arg2(), a3.arg1(),
					a3.arg2())));

	private Reservation(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
			String purpose, UUID userId) {
		reservationValidator.lazy().validated(reservationId, roomId, date, startTime, endTime, purpose, userId);
		this.reservationId = reservationId;
		this.roomId = roomId;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.purpose = purpose;
		this.userId = userId;
		this.cancelled = false;
	}

	@Builder(style = BuilderStyle.STAGED)
	public static Reservation create(UUID reservationId, UUID roomId, LocalDate date, LocalTime startTime,
			LocalTime endTime, String purpose, UUID userId) {
		return new Reservation(reservationId, roomId, date, startTime, endTime, purpose, userId);
	}

	public static Reservation fromEvents(Collection<ReservationEvent> events) {
		if (events.isEmpty()) {
			throw new IllegalArgumentException("No events provided");
		}
		Iterator<ReservationEvent> iterator = events.iterator();
		ReservationEvent event1 = iterator.next();
		if (event1 instanceof ReservationConfirmedEvent confirmedEvent) {
			Reservation reservation = create(confirmedEvent.getReservationId(), confirmedEvent.getRoomId(),
					confirmedEvent.getDate(), confirmedEvent.getStartTime(), confirmedEvent.getEndTime(),
					confirmedEvent.getPurpose(), confirmedEvent.getUserId());
			if (iterator.hasNext()) {
				DomainEvent event2 = iterator.next();
				if (event2 instanceof ReservationCancelledEvent) {
					reservation.cancel();
				}
				else {
					throw new IllegalArgumentException("second event is not a ReservationCancelledEvent");
				}
			}
			return reservation;
		}
		else {
			throw new IllegalStateException("first event is not a ReservationConfirmedEvent");
		}

	}

	void cancel() {
		this.cancelled = true;
	}

	public UUID getReservationId() {
		return reservationId;
	}

	public UUID getRoomId() {
		return roomId;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getPurpose() {
		return purpose;
	}

	public UUID getUserId() {
		return userId;
	}

	public boolean isCancelled() {
		return cancelled;
	}

}
