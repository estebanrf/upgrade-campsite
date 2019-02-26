package com.upgrade.model.mapper;

import com.upgrade.model.dao.Reservation;

public interface ReservationMapper {

	Reservation toReservation(com.upgrade.model.Reservation reservation);

	com.upgrade.model.Reservation toReservation(com.upgrade.model.dao.Reservation reservation);

}
