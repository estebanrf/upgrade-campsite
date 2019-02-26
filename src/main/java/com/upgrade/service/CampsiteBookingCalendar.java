package com.upgrade.service;

import static com.upgrade.repository.queries.QueryUtils.reservationsOverlappingIntervalQuery;
import static com.upgrade.service.BookingCalendarErrorMessages.CAMPSITE_IS_BEING_USED_MESSAGE;
import static com.upgrade.service.BookingCalendarErrorMessages.FROM_AT_LEAST_ONE_DAY_IN_FUTURE;
import static com.upgrade.service.BookingCalendarErrorMessages.FROM_BEFORE_TO_MESSAGE;
import static com.upgrade.service.BookingCalendarErrorMessages.INVALID_FROM_TO_COMBINATION;
import static com.upgrade.service.BookingCalendarErrorMessages.NO_RESERVATION_WITH_UUID;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.upgrade.model.Reservation;
import com.upgrade.model.Status;
import com.upgrade.model.mapper.ReservationMapper;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.repository.ReservationRepository;
import com.upgrade.service.constraints.BookableReservationChecker;

@Log4j
@Component("campsiteBookingCalendar")
public class CampsiteBookingCalendar {

	private static final int DEFAULT_FROM_DAYS_LATER_AVAILABILITY = 1;
	private static final int DEFAULT_TO_DAYS_LATER_AVAILABILITY = 30;

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private ReservationMapper reservationMapper;
	@Autowired
	private BookableReservationChecker bookableReservationChecker;

	@Transactional
	public String bookCampsite(BookingRequest request) {
		final Reservation reservation = Reservation.between(request.getFrom(), request.getTo(), request.getGuestEmail(), request.getGuestFirstName(), request.getGuestLastName());

		bookableReservationChecker.checkForCampsiteBookingConstraints(reservation);

		final Date from = reservation.getFrom();
		final Date to = reservation.getTo();

		final Runnable reservationRunnable = () -> {
			reservationRepository.findAll(reservationsOverlappingIntervalQuery(from, to)).stream().findFirst()
			.ifPresent(overlappedReservation -> {throw new IllegalStateException(CAMPSITE_IS_BEING_USED_MESSAGE);});

			final com.upgrade.model.dao.Reservation newReservationForUser = reservationMapper.toReservation(reservation);
				reservationRepository.saveAndFlush(newReservationForUser);
		};

		saveReservationSync(reservationRunnable);
		return reservation.getUuid();
	}

	@Transactional(readOnly = true)
	public List<List<Date>> checkAvailability(Long from, Long to) {

		final Date rangeFrom = (from == null ? DateTime.now().plusDays(DEFAULT_FROM_DAYS_LATER_AVAILABILITY) : new DateTime(from))
			.withTime(12, 0, 0, 0).toDate();

		final Date rangeTo = (to == null ? DateTime.now().plusDays(DEFAULT_TO_DAYS_LATER_AVAILABILITY) : new DateTime(to))
			.withTime(12, 0, 0, 0).toDate();

		if (rangeFrom.after(rangeTo)) {
			throw new IllegalArgumentException(INVALID_FROM_TO_COMBINATION + FROM_BEFORE_TO_MESSAGE);
		}

		if (rangeFrom.before(DateTime.now().plusDays(1).withTime(12, 0, 0, 0).toDate())) {
			throw new IllegalArgumentException(FROM_AT_LEAST_ONE_DAY_IN_FUTURE);
		}

		return bookableFreeIntervalsInDateRange(rangeFrom, rangeTo);
	}

	@Transactional
	public String updateReservation(String reservationUuid, ReservationUpdateRequest reservationUpdate) {
		final Runnable reservationRunnable = () -> {

			final com.upgrade.model.dao.Reservation reservationByUuid = requireConfirmedReservationByUuid(reservationUuid);

			final Date newFrom = new DateTime(reservationUpdate.getFrom()).withTime(12, 0, 0, 0).toDate();

			final Date newTo = new DateTime(reservationUpdate.getTo()).withTime(11, 59, 59, 999).toDate();

			reservationByUuid.setFrom(newFrom);
			reservationByUuid.setTo(newTo);

			bookableReservationChecker.checkForCampsiteBookingConstraints(reservationMapper.toReservation(reservationByUuid));

			if (reservationRepository.findAll(reservationsOverlappingIntervalQuery(newFrom, newTo)).stream()
				.anyMatch(reservation -> !reservation.getUuid().equals(reservationUuid))) {
				throw new IllegalStateException(CAMPSITE_IS_BEING_USED_MESSAGE);
			}
			reservationRepository.saveAndFlush(reservationByUuid);
		};
		saveReservationSync(reservationRunnable);

		return reservationUuid;
	}

	@Transactional
	public void cancelReservation(String reservationUuid) {
		final Runnable reservationRunnable = () -> {
			final com.upgrade.model.dao.Reservation reservationByUuid = requireConfirmedReservationByUuid(reservationUuid);

			reservationByUuid.setStatus(Status.CANCELLED);
			reservationRepository.saveAndFlush(reservationByUuid);
		};
		saveReservationSync(reservationRunnable);
	}

	@Transactional(readOnly = true)
	public List<Reservation> readReservations() {
		return reservationRepository.findAll().stream()
			.map(res -> reservationMapper.toReservation(res))
			.collect(toList());
	}

	private List<List<Date>> bookableFreeIntervalsInDateRange(Date availabilityFrom, Date availabilityTo) {

		final List<com.upgrade.model.dao.Reservation> sortedReservationsInPeriod = reservationRepository.findAll(reservationsOverlappingIntervalQuery(availabilityFrom, availabilityTo)).stream()
			.sorted((res1, res2) -> res1.getFrom().before(res2.getFrom()) ? 1 : 0).collect(toList());

		if (sortedReservationsInPeriod.isEmpty()) {
			return Collections.singletonList(Arrays.asList(availabilityFrom, availabilityTo));
		}

		Date currentFreeGapBeginning = availabilityFrom;
		final List<List<Date>> freeGaps = new LinkedList<>();

		for (com.upgrade.model.dao.Reservation reservation : sortedReservationsInPeriod) {
			if (currentFreeGapBeginning.before(reservation.getFrom())) {
				freeGaps.add(Arrays.asList(availabilityFrom, reservation.getFrom()));
			}
			currentFreeGapBeginning = new DateTime(reservation.getTo().toInstant().toEpochMilli()).withTime(12,0,0,0).toDate();
		}
		if (currentFreeGapBeginning.before(availabilityTo)) {
			freeGaps.add(Arrays.asList(currentFreeGapBeginning, availabilityTo));
		}

		return freeGaps;
	}

	private com.upgrade.model.dao.Reservation requireConfirmedReservationByUuid(String reservationUuid) {
		final com.upgrade.model.dao.Reservation reservationByUuid = reservationRepository.findByUuid(reservationUuid);

		if (reservationByUuid == null || !Status.CONFIRMED.equals(reservationByUuid.getStatus())) {
			throw new IllegalStateException(NO_RESERVATION_WITH_UUID + reservationUuid);
		}
		return reservationByUuid;
	}

	private synchronized void saveReservationSync(Runnable reservationRunnable) {
		reservationRunnable.run();
	}
}
