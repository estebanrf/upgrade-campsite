package com.upgrade.rest;

import javax.validation.Valid;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.model.requests.BookingRequest;
import com.upgrade.model.requests.ReservationUpdateRequest;
import com.upgrade.service.CampsiteBookingCalendar;

@RestController
@RequestMapping(path = "/api/campsite/v1")
public class CampsiteController {

	@Autowired
	private CampsiteBookingCalendar campsiteBookingCalendar;

	@PostMapping(path= "/reservations", consumes = "application/json", produces = "application/json")
	public @ResponseBody ResponseEntity makeReservation(@RequestBody @Valid BookingRequest bookingRequest) {
		return ResponseEntity.ok(campsiteBookingCalendar.bookCampsite(bookingRequest));
	}

	@GetMapping(path="/availability", produces = "application/json")
	public @ResponseBody ResponseEntity checkAvailability(@QueryParam("from") Long from, @QueryParam("to") Long to) {
		return ResponseEntity.ok().body(campsiteBookingCalendar.checkAvailability(from, to));
	}

	@PatchMapping(path="/reservations/{reservationId}", produces = "application/json")
	public @ResponseBody ResponseEntity updateReservation(@PathVariable("reservationId") String reservationId, @RequestBody @Valid ReservationUpdateRequest reservationUpdateRequest) {
		return ResponseEntity.ok().body(campsiteBookingCalendar.updateReservation(reservationId, reservationUpdateRequest));
	}

	@PostMapping(path="/reservations/{reservationId}/cancel", produces = "application/json")
	public ResponseEntity cancelReservation(@PathVariable("reservationId") String reservationId) {
		campsiteBookingCalendar.cancelReservation(reservationId);
		return ResponseEntity.noContent().build();
	}

	/* This GET method was added just for checking created and updated reservations, this wasn't required by requirements specified in PDF file */
	@GetMapping(path="/reservations", produces = "application/json")
	public ResponseEntity readReservations() {
		return ResponseEntity.ok(campsiteBookingCalendar.readReservations());
	}
}
