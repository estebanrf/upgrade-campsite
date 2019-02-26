package com.upgrade.service.constraints;


import static com.upgrade.service.BookingCalendarErrorMessages.BOOKING_RESTRICTIONS_ERROR_MESSAGE_PREFIX;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.upgrade.model.Reservation;
import com.upgrade.service.constraints.impl.BookUpToThreeDaysConstraint;
import com.upgrade.service.constraints.impl.BookUpWithinAMonthBeforeConstraint;
import com.upgrade.service.constraints.impl.FromAfterNowConstraint;
import com.upgrade.service.constraints.restrictions.BookingConstraint;

@Component
public class BookableReservationChecker {

	private List<BookableReservationConstraint> rules = new LinkedList<>();

	public BookableReservationChecker() {
		rules.add(new BookUpToThreeDaysConstraint());
		rules.add(new BookUpWithinAMonthBeforeConstraint());
		rules.add(new FromAfterNowConstraint());
	}

	public void checkForCampsiteBookingConstraints(Reservation reservation) {
		final List<BookingConstraint> restrictions = rules.stream().map(rule -> rule.apply(reservation)).filter(restriction -> !restriction.equals(BookingConstraint.NONE)).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(restrictions)) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append(BOOKING_RESTRICTIONS_ERROR_MESSAGE_PREFIX + " [ ");
			restrictions.forEach(restriction -> errorMessage.append(restriction.getDescription() + " (" + restriction.name() + ").").append("  "));
			errorMessage.append("]");

			throw new IllegalStateException(errorMessage.toString());
		}
	}
}
