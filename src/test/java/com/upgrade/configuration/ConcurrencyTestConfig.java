package com.upgrade.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.upgrade.model.mapper.ReservationMapper;
import com.upgrade.model.mapper.impl.ReservationMapperImpl;
import com.upgrade.service.CampsiteBookingCalendar;
import com.upgrade.service.constraints.BookableReservationChecker;


@Configuration
@EnableAutoConfiguration(exclude = {SecurityFilterAutoConfiguration.class})
public class ConcurrencyTestConfig {

	@Bean
	public CampsiteBookingCalendar campsiteReservationCalendar() {
		return new CampsiteBookingCalendar();
	}

	@Bean
	public ReservationMapper reservationMapper() {
		ReservationMapperImpl reservationMapper = new ReservationMapperImpl();
		reservationMapper.init();
		return reservationMapper;
	}

	@Bean
	public BookableReservationChecker bookableReservationChecker() {
		return new BookableReservationChecker();
	}
}
