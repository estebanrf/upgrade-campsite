package com.upgrade.service.constraints.impl;

import static com.upgrade.service.constraints.restrictions.BookingConstraint.FROM_DATE_IS_IN_THE_PAST;
import static com.upgrade.service.constraints.restrictions.BookingConstraint.NONE;

import java.util.Date;

import org.joda.time.DateTime;

import com.upgrade.model.Reservation;
import com.upgrade.service.constraints.BookableReservationConstraint;
import com.upgrade.service.constraints.restrictions.BookingConstraint;

public class FromAfterNowConstraint implements BookableReservationConstraint {

	@Override
	public BookingConstraint apply(Reservation reservation) {
		Date now = DateTime.now().toDate();

		return reservation.getFrom().before(now) ? FROM_DATE_IS_IN_THE_PAST : NONE;
	}
}
