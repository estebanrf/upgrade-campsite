package com.upgrade.repository.queries;

import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import com.upgrade.model.Status;
import com.upgrade.model.dao.Reservation;

public class QueryUtils {

	public static Specification<Reservation> reservationsOverlappingIntervalQuery(Date intervalBeginning, Date intervalEnding) {

		ReservationSpecification specFromIncluded = new ReservationSpecification(new SearchCriteria("from", "<", intervalBeginning));
		ReservationSpecification spec2FromIncluded = new ReservationSpecification(new SearchCriteria("to", ">", intervalBeginning));
		ReservationSpecification specToIncluded = new ReservationSpecification(new SearchCriteria("from", "<", intervalEnding));
		ReservationSpecification spec2ToIncluded = new ReservationSpecification(new SearchCriteria("to", ">", intervalEnding));
		ReservationSpecification specIntervalIncludes = new ReservationSpecification(new SearchCriteria("from", ">", intervalBeginning));
		ReservationSpecification spec3IntervalIncludes = new ReservationSpecification(new SearchCriteria("to", "<", intervalEnding));
		ReservationSpecification specIntervalIsIncluded = new ReservationSpecification(new SearchCriteria("from", "<", intervalBeginning));
		ReservationSpecification spec4IntervalIsIncluded = new ReservationSpecification(new SearchCriteria("to", ">", intervalEnding));
		ReservationSpecification spec5 = new ReservationSpecification(new SearchCriteria("status", ":", Status.CONFIRMED));

		return Specification.where(specFromIncluded.and(spec2FromIncluded)
			.or(specToIncluded.and(spec2ToIncluded))
			.or(specIntervalIncludes.and(spec3IntervalIncludes))
			.or(specIntervalIsIncluded.and(spec4IntervalIsIncluded)))
			.and(spec5);
	}
}
