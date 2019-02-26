package com.upgrade.service;

public interface BookingCalendarErrorMessages {
	String CAMPSITE_IS_BEING_USED_MESSAGE = "Can't make reservation, campsite is being used.";
	String INVALID_FROM_TO_COMBINATION = "Invalid from/to combination: ";
	String FROM_BEFORE_TO_MESSAGE = "From must be before To.";
	String FROM_AT_LEAST_ONE_DAY_IN_FUTURE = "From date must be at least one day in the future.";
	String NO_RESERVATION_WITH_UUID = "No reservation with UUID: ";
	String BOOKING_RESTRICTIONS_ERROR_MESSAGE_PREFIX = "Booking restrictions for this reservation are:";

}
