package eu.transparency.lobbycal.web.rest;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.User;

final class MeetingSpecifications {
	private static final Logger log = LoggerFactory
			.getLogger(MeetingSpecifications.class);

	MeetingSpecifications() {
	}

	static Specification<Meeting> hasTitle(String title, String mepIds) {
		return new Specification<Meeting>() {
			@Override
			public Predicate toPredicate(Root<Meeting> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {

				Subquery<Meeting> sq = query.subquery(Meeting.class);
				Root<User> user = sq.from(User.class);
				Join<User, Meeting> sqEmp = user.join("id");
				sq.select(sqEmp).where(
						cb.like(user.get("id"),
								cb.parameter(String.class, "9")));
				return cb.in(root).value(sq);

				// log.info(title);
				// List<Predicate> can = new ArrayList<>();
				//
				// String containsLikePattern = getContainsLikePattern(title);
				// log.info("" + root.<User> get("user").<String>
				// get("lastName"));
				//
				// try {
				// // can criteria
				// can.add(cb.like(cb.lower(root.<String> get("title")),
				// containsLikePattern));
				// can.add(cb.like(cb.lower(root.<String> get("mPartner")),
				// containsLikePattern));
				// can.add(cb.like(cb.lower(root.<String> get("mTag")),
				// containsLikePattern));
				// // // must criteria
				// // Predicate must = cb.in(cb.lower(root.<User>
				// get("user").<String> get("lastName")), mepIds);
				//
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// return orTogether(can, cb);
			}

		};

	}

	private static Predicate orTogether(List<Predicate> predicates,
			CriteriaBuilder cb) {
		return cb.or(predicates.toArray(new Predicate[0]));
	}

	private static String getContainsLikePattern(String searchTerm) {
		if (searchTerm == null || searchTerm.isEmpty()) {
			log.info(searchTerm);
			return "%";
		} else {
			log.info(searchTerm);
			return "%" + searchTerm.toLowerCase() + "%";
		}
	}
}