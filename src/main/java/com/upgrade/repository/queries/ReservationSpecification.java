package com.upgrade.repository.queries;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import lombok.AllArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import com.upgrade.model.dao.Reservation;

@AllArgsConstructor
public class ReservationSpecification implements Specification<Reservation> {

	private SearchCriteria criteria;

	@Nullable
	@Override
	public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
		if (criteria.getOperation().equalsIgnoreCase(">")) {
			return criteriaBuilder.greaterThanOrEqualTo(
				root.<Date> get(criteria.getKey()), (Date) criteria.getValue());
		}
		else if (criteria.getOperation().equalsIgnoreCase("<")) {
			return criteriaBuilder.lessThanOrEqualTo(
				root.<Date> get(criteria.getKey()), (Date) criteria.getValue());
		}
		else if (criteria.getOperation().equalsIgnoreCase(":")) {
			if (root.get(criteria.getKey()).getJavaType() == String.class) {
				return criteriaBuilder.like(
					root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
			} else {
				return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
			}
		}
		return null;
	}
}
