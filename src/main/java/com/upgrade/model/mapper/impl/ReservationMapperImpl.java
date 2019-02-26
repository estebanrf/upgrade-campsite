package com.upgrade.model.mapper.impl;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.upgrade.model.dao.Reservation;
import com.upgrade.model.mapper.ReservationMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

@Component
public class ReservationMapperImpl implements ReservationMapper {

	private MapperFacade facade;

	@PostConstruct
	public void init() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		mapperFactory.classMap(Reservation.class, com.upgrade.model.Reservation.class)
			.byDefault().register();
		facade = mapperFactory.getMapperFacade();
	}

	@Override
	public Reservation toReservation(com.upgrade.model.Reservation reservation) {
		return facade.map(reservation, Reservation.class);
	}

	@Override
	public com.upgrade.model.Reservation toReservation(Reservation reservation) {
		return facade.map(reservation, com.upgrade.model.Reservation.class);
	}
}
