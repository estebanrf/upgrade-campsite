package com.upgrade.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import com.upgrade.factory.CampsiteFactory;
import com.upgrade.model.mapper.ReservationMapper;
import com.upgrade.model.mapper.impl.ReservationMapperImpl;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.repository.ReservationRepository;
import com.upgrade.service.CampsiteBookingCalendar;
import com.upgrade.service.constraints.BookableReservationChecker;

public class CampsiteBookingCalendarTest {

	@Spy
	private BookableReservationChecker bookableReservationChecker;
	@Spy
	private ReservationMapper reservationMapper;
	@Mock
	private ReservationRepository reservationRepository;
	@InjectMocks
	private CampsiteBookingCalendar campsiteBookingCalendar;

	@Before
	public void setup() {
		final ReservationMapperImpl reservationMapperImpl = new ReservationMapperImpl();
		reservationMapperImpl.init();
		this.reservationMapper = reservationMapperImpl;
		initMocks(this);
	}

	@Test
	public void test_newCampsite_hasNoReservations() {

		when(reservationRepository.findAll()).thenReturn(emptyList());
		Assertions.assertThat(CollectionUtils.isEmpty(campsiteBookingCalendar.readReservations()));
	}

	@Test
	public void test_campsite_makeReservation_validDates_newReservationUuid() {
		final BookingRequest request = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		final String reservationUuid = campsiteBookingCalendar.bookCampsite(request);

		Assertions.assertThat(reservationUuid != null).isTrue();
	}

	@Test(expected = IllegalStateException.class)
	public void test_campsite_canNotMakeOverlappedReservations_mustThrowIllegalStateException() throws Exception {
		final BookingRequest request = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>) any())).thenReturn(singletonList(CampsiteFactory.persistedReservationFromTomorrowToDayAfterTomorrow()));

		campsiteBookingCalendar.bookCampsite(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_campsite_availabilityFromAfterTo_noReservationsWereMade_mustThrowIllegalArgumentException() throws Exception {
		final long to = CampsiteFactory.tomorrowAtMiddayInMillis();
		final long from = CampsiteFactory.dayAfterTomorrowBeforeMiddayInMillis();

		campsiteBookingCalendar.checkAvailability(from, to);
	}

	@Test
	public void test_campsite_availabilityOneMonthInFutureDefault_noReservationsWereMade_aMonthFromTomorrowFree() {
		final List<List<Date>> availableIntervals = campsiteBookingCalendar.checkAvailability(null, null);

		Assertions.assertThat(availableIntervals.isEmpty()).isFalse();

		Date availabilityFrom = DateTime.now().plusDays(1).withTime(12, 0, 0, 0).toDate();
		final List<Date> availableSegment = availableIntervals.stream().findFirst().get();
		Assertions.assertThat(availableSegment.get(0).equals(availabilityFrom)).isTrue();

		Date availabilityTo = DateTime.now().plusDays(30).withTime(12, 0, 0, 0).toDate();
		Assertions.assertThat(availableSegment.get(1).equals(availabilityTo)).isTrue();

	}

	@Test
	public void test_campsite_availabilityOneMonthInFutureDefault_reservationWasMadeAtTheBeginning_notTheWholeMonthAvailable() {
		final com.upgrade.model.dao.Reservation reservation = CampsiteFactory.persistedReservationFromTomorrowToDayAfterTomorrow();
		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>)any())).thenReturn(singletonList(reservation));
		final List<List<Date>> availableIntervals = campsiteBookingCalendar.checkAvailability(null, null);

		Assertions.assertThat(availableIntervals.size() == 1).isTrue();
		final List<Date> availableSegment = availableIntervals.stream().findFirst().get();

		final Date expectedAvailableSegmentStart = new DateTime(reservation.getTo()).withTime(12, 0, 0, 0).toDate();
		Assertions.assertThat(expectedAvailableSegmentStart.equals(availableSegment.get(0)));

		final Date expectedAvailableSegmentEnd = new DateTime(reservation.getFrom()).plusDays(30).withTime(12, 0, 0, 0).toDate();

		Assertions.assertThat(expectedAvailableSegmentEnd.equals(availableSegment.get(1)));
	}

	@Test
	public void test_campsite_availabilityOneMonthInFutureDefault_reservationWasMadeInTheEnd_notTheWholeMonthAvailable() {
		final com.upgrade.model.dao.Reservation reservation = CampsiteFactory.persistedReservationInTheEndOfDefaultAvailabilityRange();
		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>)any())).thenReturn(singletonList(reservation));
		final List<List<Date>> availableIntervals = campsiteBookingCalendar.checkAvailability(null, null);

		Assertions.assertThat(availableIntervals.size() == 1).isTrue();
		final List<Date> availableSegment = availableIntervals.stream().findFirst().get();

		final Date expectedAvailableSegmentStart = DateTime.now().plusDays(1).withTime(12, 0, 0, 0).toDate();
		Assertions.assertThat(expectedAvailableSegmentStart.equals(availableSegment.get(0)));

		final Date expectedAvailableSegmentEnd = reservation.getTo();

		Assertions.assertThat(expectedAvailableSegmentEnd.equals(availableSegment.get(1)));

	}

	@Test
	public void test_campsite_availabilityOneMonthInFutureDefault_reservationWasMadeInTheMiddle_notTheWholeMonthAvailable() {
		final com.upgrade.model.dao.Reservation reservation = CampsiteFactory.persistedReservationInTheMiddleOfTheDefaultAvailabilityRange();

		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>)any())).thenReturn(singletonList(reservation));
		final List<List<Date>> availableIntervals = campsiteBookingCalendar.checkAvailability(null, null);

		Assertions.assertThat(availableIntervals.size()).isEqualTo(2);

		final List<Date> firstAvailableSegment = availableIntervals.get(0);

		final Date firstAvailableSegmentStart = firstAvailableSegment.get(0);
		Date expectedFirstAvailableSegmentFrom = DateTime.now().plusDays(1).withTime(12, 0, 0, 0).toDate();
		Assertions.assertThat(firstAvailableSegmentStart.equals(expectedFirstAvailableSegmentFrom)).isTrue();

		final Date firstAvailableSegmentEnd = firstAvailableSegment.get(1);
		Date expectedFirstAvailableSegmentTo = reservation.getFrom();
		Assertions.assertThat(firstAvailableSegmentEnd.equals(expectedFirstAvailableSegmentTo)).isTrue();

		final List<Date> secondAvailableSegment = availableIntervals.get(1);

		final Date secondAvailableSegmentStart = secondAvailableSegment.get(0);
		Date expectedSecondAvailableSegmentFrom = reservation.getTo();
		Assertions.assertThat(secondAvailableSegmentStart.equals(expectedSecondAvailableSegmentFrom)).isTrue();

		final Date secondAvailableSegmentEnd = secondAvailableSegment.get(1);
		Date expectedSecondAvailableSegmentTo = DateTime.now().plusDays(30).withTime(12, 0, 0, 0).toDate();
		Assertions.assertThat(secondAvailableSegmentEnd.equals(expectedSecondAvailableSegmentTo)).isTrue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_campsite_availabilityToBeforeFrom_mustThrowIllegalArgumentException() throws Exception {
		final DateTime now = DateTime.now();
		final long from = now.plusDays(3).getMillis();
		final long to = now.plusDays(2).getMillis();
		campsiteBookingCalendar.checkAvailability(from, to);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_campsite_updateFromDateInPast_onExistingReservation_mustThrowIllegalArgumentException() throws Exception {
		final DateTime now = DateTime.now();
		final long from = now.minusDays(1).getMillis();
		final long to = now.plusDays(1).getMillis();
		campsiteBookingCalendar.checkAvailability(from, to);

	}

	@Test(expected = IllegalStateException.class)
	public void test_campsite_update_onNonExistingReservation_throwsIllegalStateException() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		final ReservationUpdateRequest reservationUpdate = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();
		when(reservationRepository.findByUuid(reservationUuid)).thenReturn(null);

		campsiteBookingCalendar.updateReservation(reservationUuid, reservationUpdate);
	}

	@Test(expected = IllegalStateException.class)
	public void test_campsite_update_onExistingReservation_overlapsAnother_throwsIllegalStateException() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		final ReservationUpdateRequest reservationUpdate = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();

		when(reservationRepository.findByUuid(reservationUuid)).thenReturn(CampsiteFactory.persistedReservationInTheMiddleOfTheDefaultAvailabilityRange());
		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>)any())).thenReturn(singletonList(CampsiteFactory.persistedReservationFromTomorrowToDayAfterTomorrow()));
		campsiteBookingCalendar.updateReservation(reservationUuid, reservationUpdate);
	}

	@Test
	public void test_campsite_update_onExistingReservation_ok() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		final ReservationUpdateRequest reservationUpdate = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();

		when(reservationRepository.findByUuid(reservationUuid)).thenReturn(CampsiteFactory.persistedReservationInTheMiddleOfTheDefaultAvailabilityRange());
		when(reservationRepository.findAll((Specification<com.upgrade.model.dao.Reservation>)any())).thenReturn(emptyList());

		final String updatedReservationUuid = campsiteBookingCalendar.updateReservation(reservationUuid, reservationUpdate);
		Assertions.assertThat(updatedReservationUuid.equals(reservationUuid)).isTrue();
	}

	@Test
	public void test_campsite_cancelReservation_onExistingReservation_ok() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		when(reservationRepository.findByUuid(reservationUuid)).thenReturn(CampsiteFactory.persistedReservationInTheMiddleOfTheDefaultAvailabilityRange());

		campsiteBookingCalendar.cancelReservation(reservationUuid);
		verify(reservationRepository).saveAndFlush(any());
	}

	@Test(expected = IllegalStateException.class)
	public void test_campsite_cancelReservation_onNonExistingReservation_throwsIllegalStateException() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		when(reservationRepository.findByUuid(reservationUuid)).thenReturn(null);

		campsiteBookingCalendar.cancelReservation(reservationUuid);
	}

}
