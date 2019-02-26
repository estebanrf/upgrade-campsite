package com.upgrade.model.requests;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class ReservationUpdateRequest {

	@NonNull
	@NotNull
	private Long from;
	@NonNull
	@NotNull
	private Long to;
}
