package com.upgrade.model.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class BookingRequest {

	@NonNull
	@NotNull
	private Long from;
	@NonNull
	@NotNull
	private Long to;
	@NonNull
	@NotBlank
	@Email
	private String guestEmail;
	@NonNull
	@NotBlank
	private String guestFirstName;
	@NonNull
	@NotBlank
	private String guestLastName;

}
