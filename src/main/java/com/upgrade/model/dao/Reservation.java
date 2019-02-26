package com.upgrade.model.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.upgrade.model.Status;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
	private String uuid;

	@Column(name = "from_date")
	private Date from;

	@Column(name = "to_date")
	private Date to;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(name = "guest_email", nullable = false, length = 50)
	private String guestEmail;

	@Column(name = "guest_first_name", length = 50)
	private String guestFirstName;

	@Column(name = "guest_last_name", length = 50)
	private String guestLastName;
}
