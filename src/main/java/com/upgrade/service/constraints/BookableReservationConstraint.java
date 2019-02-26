package com.upgrade.service.constraints;

import com.upgrade.model.Reservation;
import com.upgrade.service.constraints.restrictions.BookingConstraint;

public interface BookableReservationConstraint {

	BookingConstraint apply(Reservation reservation);
}
