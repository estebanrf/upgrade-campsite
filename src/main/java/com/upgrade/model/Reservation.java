package com.upgrade.model;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.joda.time.DateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

	private String uuid;
	private Date from;
	private Date to;
	private Status status;
	private String guestEmail;
	private String guestFirstName;
	private String guestLastName;

	public static Reservation between(Long fromDate, Long toDate, String guestEmail, String guestFirstName, String guestLastName) {
		Date from = new DateTime(fromDate).withTime(12, 0, 0, 0).toDate();
		Date to = new DateTime(toDate).withTime(11, 59, 59,999).toDate();

		if (from.after(to)) {
			throw new IllegalArgumentException("Invalid dates: from is after to.");
		}
		return Reservation.builder().uuid(UUID.randomUUID().toString()).from(from).to(to).status(Status.CONFIRMED).guestEmail(guestEmail).guestFirstName(guestFirstName).guestLastName(guestLastName).build();
	}

}
