package com.upgrade.service.constraints.restrictions;

public enum BookingConstraint {

	NONE(""), MORE_THAN_3_DAYS_RESERVATION("Can not book for more than three days"), MUST_BOOK_UP_UNTIL_A_MONTH_BEFORE("Must book up until a month before from date"), FROM_DATE_IS_IN_THE_PAST("From date is in the past");

	private final String description;

	BookingConstraint(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
