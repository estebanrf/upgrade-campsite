package com.upgrade.service.constraints.impl;

import static com.upgrade.service.constraints.restrictions.BookingConstraint.MORE_THAN_3_DAYS_RESERVATION;

import org.joda.time.Interval;

import com.upgrade.model.Reservation;
import com.upgrade.service.constraints.BookableReservationConstraint;
import com.upgrade.service.constraints.restrictions.BookingConstraint;

public class BookUpToThreeDaysConstraint implements BookableReservationConstraint {

	@Override
	public BookingConstraint apply(Reservation reservation) {
		final Interval reservationInterval = new Interval(reservation.getFrom().toInstant().toEpochMilli(), reservation.getTo().toInstant().toEpochMilli() + 1);
		final long daysOfReservation = reservationInterval.toDuration().getStandardDays();

		return  daysOfReservation > 3 ? MORE_THAN_3_DAYS_RESERVATION : BookingConstraint.NONE;
	}
}
