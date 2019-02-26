package com.upgrade.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.configuration.ConcurrencyTestConfig;
import com.upgrade.model.mapper.ReservationMapper;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.service.CampsiteBookingCalendar;
import com.upgrade.service.constraints.BookableReservationChecker;

@Import(ConcurrencyTestConfig.class)
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles(profiles = { "test" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CampsiteBookingCalendarConcurrencyTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static int NUM_THREADS = 300;
	@Spy
	private BookableReservationChecker bookableReservationChecker;
	@Spy
	private ReservationMapper reservationMapper;
	@Autowired
	private CampsiteBookingCalendar campsiteBookingCalendar;

	@Test
	public void test_campsite_makeReservations300Users_tillItsFullInPeriod_max30ReservationsCanBeMade() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		final int[] rejectedBookingAttempts = {0};

		final DateTime now = DateTime.now();

		for (int threadNum = 0; threadNum < NUM_THREADS; threadNum++) {
			executor.submit(() -> {

				for (int plusDays = 1; plusDays <= 30; plusDays++) {
					DateTime from = now.plusDays(plusDays).withTime(12, 0, 0, 0);
					DateTime to = from.plusDays(1).withTime(11, 59, 59, 999);
					final BookingRequest reservationRequest = BookingRequest.builder().from(from.getMillis()).to(to.getMillis()).guestFirstName("John").guestLastName("Wick").guestEmail("email@upgrade.com").build();
					try {
						campsiteBookingCalendar.bookCampsite(reservationRequest);
					} catch (IllegalStateException e) {
						rejectedBookingAttempts[0]++;
					}
				}
			});
		}

		executor.shutdown();
		executor.awaitTermination(50, TimeUnit.SECONDS);
		Assertions.assertThat(campsiteBookingCalendar.readReservations().size()).isEqualTo(30);
		Assertions.assertThat(rejectedBookingAttempts[0]).isEqualTo(NUM_THREADS * 30 - 30);
	}

	@Test
	public void test_campsite_usersUpdateSameReservations_noUpdateIsRejected_onlyOneReservationIsKept() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		final int[] rejectedBookingAttempts = {0};

		final DateTime now = DateTime.now();

		DateTime initialFrom = now.plusDays(1).withTime(12, 0, 0, 0);
		DateTime initialTo = initialFrom.plusDays(1).withTime(11, 59, 59, 999);
		final BookingRequest reservationRequest = BookingRequest.builder().from(initialFrom.getMillis()).to(initialTo.getMillis()).guestFirstName("John").guestLastName("Wick").guestEmail("email@upgrade.com").build();

		final String[] reservationUuid = new String[1];
			executor.submit(() -> reservationUuid[0] = campsiteBookingCalendar.bookCampsite(reservationRequest));
			executor.awaitTermination(3, TimeUnit.SECONDS);

		for (int threadNum = 0; threadNum < NUM_THREADS; threadNum++) {
			executor.submit(() -> {

				for (int plusDays = 1; plusDays <= 29; plusDays++) {
					DateTime from = initialFrom.plusDays(plusDays).withTime(12, 0, 0, 0);
					DateTime to = from.plusDays(1).withTime(11, 59, 59, 999);
					final ReservationUpdateRequest reservationUpdateRequest = ReservationUpdateRequest.builder().from(from.getMillis()).to(to.getMillis()).build();
					try {
						campsiteBookingCalendar.updateReservation(reservationUuid[0], reservationUpdateRequest);
					} catch (IllegalStateException e) {
						rejectedBookingAttempts[0]++;
					}
				}
			});
		}

		executor.shutdown();
		executor.awaitTermination(50, TimeUnit.SECONDS);
		Assertions.assertThat(campsiteBookingCalendar.readReservations().size()).isEqualTo(1);
		Assertions.assertThat(rejectedBookingAttempts[0]).isEqualTo(0);
	}

}
