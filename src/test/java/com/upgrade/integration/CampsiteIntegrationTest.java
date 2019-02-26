package com.upgrade.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upgrade.configuration.IntegrationTestConfig;
import com.upgrade.factory.CampsiteFactory;
import com.upgrade.model.dao.Reservation;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.repository.ReservationRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestConfig.class,
	webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles(profiles = { "test" })
public class CampsiteIntegrationTest {

	public static final String API_CAMPSITE_V1_BASE_PATH = "/api/campsite/v1/";
	@Autowired
	private MockMvc mvc;
	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void givenNoReservations_whenCheckingAvailability_thenReturnOneSegmentFree() throws Exception {

		final MvcResult mvcResult = mvc.perform(get(API_CAMPSITE_V1_BASE_PATH + "availability").with(csrf())
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();

		Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isNotNull();
	}

	@Test
	public void givenNoReservation_whenBooking_thenReturnUuid() throws Exception {

		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		ObjectWriter ow = objectWriterForPayload();
		final MvcResult mvcResult = mvc.perform(post(API_CAMPSITE_V1_BASE_PATH + "reservations").with(csrf())
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();

		final String reservationUuid = mvcResult.getResponse().getContentAsString();
		Assertions.assertThat(reservationUuid).isNotNull();
		deleteReservationWith(reservationUuid);
	}

	@Test
	public void givenNoReservation_whenBookingAndThenCancelling_thenNoContent() throws Exception {

		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		ObjectWriter ow = objectWriterForPayload();
		final MvcResult mvcResult = mvc.perform(post(API_CAMPSITE_V1_BASE_PATH + "reservations").with(csrf())
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();
		final String newReservationUuid = mvcResult.getResponse().getContentAsString();
		Assertions.assertThat(newReservationUuid).isNotNull();

		mvc.perform(post(API_CAMPSITE_V1_BASE_PATH + "reservations/" + newReservationUuid + "/cancel").with(csrf())
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isNoContent())
			.andReturn();

		deleteReservationWith(newReservationUuid);
	}

	@Test
	public void givenNoReservation_whenBookingAndThenUpdating_thenReturnSameUuid() throws Exception {

		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		ObjectWriter ow = objectWriterForPayload();
		MvcResult mvcResult = mvc.perform(post(API_CAMPSITE_V1_BASE_PATH + "reservations").with(csrf())
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();
		final String newReservationUuid = mvcResult.getResponse().getContentAsString();
		Assertions.assertThat(newReservationUuid).isNotNull();

		ReservationUpdateRequest reservationUpdateRequest = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();
		mvcResult = mvc.perform(patch(API_CAMPSITE_V1_BASE_PATH + "reservations/" + newReservationUuid).with(csrf())
			.content(ow.writeValueAsString(reservationUpdateRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();
		final String updatedReservation = mvcResult.getResponse().getContentAsString();
		Assertions.assertThat(newReservationUuid).isEqualTo(updatedReservation);

		deleteReservationWith(newReservationUuid);

	}

	public void deleteReservationWith(String reservationUuid) {
		final Reservation byUuid = reservationRepository.findByUuid(reservationUuid);
		reservationRepository.delete(byUuid);
	}

	private ObjectWriter objectWriterForPayload() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		return mapper.writer().withDefaultPrettyPrinter();
	}
}
