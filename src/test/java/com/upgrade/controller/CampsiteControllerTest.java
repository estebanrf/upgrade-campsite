package com.upgrade.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.upgrade.factory.CampsiteFactory;
import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.rest.CampsiteController;
import com.upgrade.service.CampsiteBookingCalendar;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CampsiteController.class, secure = false)
public class CampsiteControllerTest {
	@Autowired
	private MockMvc mvc;
	@MockBean
	private CampsiteBookingCalendar campsiteBookingCalendar;
	@Autowired
	private CampsiteController campsiteController;

	@Test
	public void givenReservation_whenBooking_thenReturnUuid() throws Exception {

		String reservationUuid = UUID.randomUUID().toString();
		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		when(campsiteBookingCalendar.bookCampsite(any(BookingRequest.class))).thenReturn(reservationUuid);

		ObjectWriter ow = objectWriterForPayload();

		final MvcResult mvcResult = mvc.perform(post("/api/campsite/v1/reservations")
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();

		Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(reservationUuid);
	}

	@Test
	public void givenIllegalArgumentException_whenBooking_mustBeBadRequest() throws Exception {
		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		doThrow(new IllegalArgumentException()).when(campsiteBookingCalendar).bookCampsite(any(BookingRequest.class));

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(post("/api/campsite/v1/reservations")
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isBadRequest())
			.andReturn();
	}

	@Test
	public void givenIllegalStateException_whenBooking_mustBeConflict() throws Exception {

		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		doThrow(new IllegalStateException()).when(campsiteBookingCalendar).bookCampsite(any(BookingRequest.class));

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(post("/api/campsite/v1/reservations")
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isConflict())
			.andReturn();
	}

	@Test
	public void givenReservation_whenUpdating_thenReturnUuid() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		when(campsiteBookingCalendar.updateReservation(anyString(), any(ReservationUpdateRequest.class))).thenReturn(reservationUuid);

		ObjectWriter ow = objectWriterForPayload();

		final MvcResult mvcResult = mvc.perform(patch("/api/campsite/v1/reservations/" + reservationUuid)
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andReturn();

		Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(reservationUuid);
	}

	@Test
	public void givenIllegalArgumentException_whenUpdating_mustBeBadRequest() throws Exception {
		final String reservationUuid = UUID.randomUUID().toString();
		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		doThrow(new IllegalArgumentException()).when(campsiteBookingCalendar).updateReservation(anyString(), any(ReservationUpdateRequest.class));

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(patch("/api/campsite/v1/reservations/" + reservationUuid)
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isBadRequest())
			.andReturn();
	}

	@Test
	public void givenIllegalStateException_whenUpdating_mustBeConflict() throws Exception {
		final String reservationUuid = UUID.randomUUID().toString();
		ReservationUpdateRequest reservationCreationRequest = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();

		doThrow(new IllegalStateException()).when(campsiteBookingCalendar).updateReservation(anyString(), any(ReservationUpdateRequest.class));

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(patch("/api/campsite/v1/reservations/" + reservationUuid)
			.content(ow.writeValueAsString(reservationCreationRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isConflict())
			.andReturn();
	}

	@Test
	public void givenIllegalStateException_whenCancelling_mustBeConflict() throws Exception {
		final String reservationUuid = UUID.randomUUID().toString();
		ReservationUpdateRequest reservationCreationRequest = CampsiteFactory.reservationUpdateRequestFromTomorrowToNextDay();

		doThrow(new IllegalStateException()).when(campsiteBookingCalendar).cancelReservation(anyString());

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(post("/api/campsite/v1/reservations/" + reservationUuid + "/cancel")
			.content(ow.writeValueAsString(reservationCreationRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isConflict())
			.andReturn();
	}

	@Test
	public void givenReservation_whenCancelling_thenNoContentResponse() throws Exception {
		String reservationUuid = UUID.randomUUID().toString();
		BookingRequest bookingRequest = CampsiteFactory.getBookingRequestFromTomorrowToDayAfterTomorrow();

		doNothing().when(campsiteBookingCalendar).cancelReservation((anyString()));

		ObjectWriter ow = objectWriterForPayload();

		mvc.perform(post("/api/campsite/v1/reservations/" + reservationUuid + "/cancel")
			.content(ow.writeValueAsString(bookingRequest))
			.contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isNoContent())
			.andReturn();
	}

	private ObjectWriter objectWriterForPayload() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		return mapper.writer().withDefaultPrettyPrinter();
	}
}
