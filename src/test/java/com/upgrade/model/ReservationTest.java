package com.upgrade.model;

import org.joda.time.DateTime;
import org.junit.Test;

public class ReservationTest {

	@Test(expected = IllegalArgumentException.class)
	public void test_reservationInvalidDateRange_minusOneDay_throwException() throws Exception {
		final DateTime now = DateTime.now();
		Reservation.between(now.toInstant().getMillis(), now.minusDays(1).toInstant().getMillis(), "john@upgrade.com", "John", "Wick");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_reservationValidDateRange_sameDays_throwException() throws Exception {
		final long todayMillis = DateTime.now().plusDays(1).toInstant().getMillis();
		Reservation.between(todayMillis, todayMillis, "john@upgrade.com", "John", "Wick");
	}

	@Test
	public void test_reservationValidDateRange_validDates_isOk() throws Exception {
		Reservation.between(DateTime.now().plusDays(1).toInstant().getMillis(), DateTime.now().plusDays(2).toInstant().getMillis(), "john@upgrade.com", "John", "Wick");
	}
}
