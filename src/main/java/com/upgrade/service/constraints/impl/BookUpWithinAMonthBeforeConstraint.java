package com.upgrade.service.constraints.impl;

import static com.upgrade.service.constraints.restrictions.BookingConstraint.MUST_BOOK_UP_UNTIL_A_MONTH_BEFORE;
import static com.upgrade.service.constraints.restrictions.BookingConstraint.NONE;

import org.joda.time.DateTime;

import com.upgrade.model.Reservation;
import com.upgrade.service.constraints.BookableReservationConstraint;
import com.upgrade.service.constraints.restrictions.BookingConstraint;

public class BookUpWithinAMonthBeforeConstraint implements BookableReservationConstraint {

	@Override
	public BookingConstraint apply(Reservation reservation) {
		DateTime fromMinus30Days = new DateTime(reservation.getFrom()).minusDays(30);

		DateTime fromMinus1Days = new DateTime(reservation.getFrom()).minusDays(1);

		final DateTime now = DateTime.now().withTime(12, 0, 0, 0);

		return fromMinus30Days.isAfter(now) || fromMinus1Days.isBefore(now) ? MUST_BOOK_UP_UNTIL_A_MONTH_BEFORE : NONE;
	}
}
