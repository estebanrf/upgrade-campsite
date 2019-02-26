package com.upgrade.factory;

import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;

import com.upgrade.model.Status;
import com.upgrade.model.dao.Reservation;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;

public class CampsiteFactory {

	final static private String reservationUuid = UUID.randomUUID().toString();
	public static long tomorrowAtMiddayInMillis() {
		return DateTime.now().plusDays(1).withTime(12, 0, 0, 0).getMillis();
	}

	public static long dayAfterTomorrowBeforeMiddayInMillis() {
		return DateTime.now().plusDays(2).withTimeAtStartOfDay().withTime(11, 59, 59, 99).getMillis();
	}

	public static Reservation persistedReservationFromTomorrowToDayAfterTomorrow() {
		final Reservation reservation = new Reservation();
		reservation.setFrom(new Date(tomorrowAtMiddayInMillis()));
		reservation.setTo(new Date(dayAfterTomorrowBeforeMiddayInMillis()));
		reservation.setGuestFirstName("John");
		reservation.setGuestLastName("Wick");
		reservation.setGuestEmail("john@upgrade.com");
		reservation.setUuid(reservationUuid);
		reservation.setId(1L);
		return reservation;
	}

	public static BookingRequest getBookingRequestFromTomorrowToDayAfterTomorrow() {
		return BookingRequest.builder()
			.from(tomorrowAtMiddayInMillis())
			.to(dayAfterTomorrowBeforeMiddayInMillis())
			.guestFirstName("John")
			.guestLastName("Wick")
			.guestEmail("john@upgrade.com")
			.build();
	}

	public static Reservation persistedReservationInTheMiddleOfTheDefaultAvailabilityRange() {
		final Date reservationFrom = DateTime.now().plusDays(15).withTime(12, 0, 0, 0).toDate();
		final Date reservationTo = DateTime.now().plusDays(17).withTime(12, 0, 0, 0).toDate();

		return reservationBetween(reservationFrom, reservationTo);

	}

	public static Reservation persistedReservationInTheEndOfDefaultAvailabilityRange() {
		final Date reservationFrom = DateTime.now().plusDays(29).withTime(12, 0, 0, 0).toDate();
		final Date reservationTo = DateTime.now().plusDays(31).withTime(12, 0, 0, 0).toDate();

		return reservationBetween(reservationFrom, reservationTo);
	}

	private static Reservation reservationBetween(Date from, Date to) {
		final Reservation reservation = new Reservation();
		reservation.setFrom(from);
		reservation.setTo(to);
		reservation.setGuestFirstName("John");
		reservation.setGuestLastName("Wick");
		reservation.setGuestEmail("john@upgrade.com");
		reservation.setUuid(reservationUuid);
		reservation.setId(1L);
		reservation.setStatus(Status.CONFIRMED);
		return reservation;
	}

	public static ReservationUpdateRequest reservationUpdateRequestFromTomorrowToNextDay() {
		final long reservationFrom = DateTime.now().plusDays(15).withTime(12, 0, 0, 0).getMillis();
		final long reservationTo = DateTime.now().plusDays(17).withTime(13, 59, 59, 999).getMillis();
		return ReservationUpdateRequest.builder().from(reservationFrom).to(reservationTo).build();
	}
}

