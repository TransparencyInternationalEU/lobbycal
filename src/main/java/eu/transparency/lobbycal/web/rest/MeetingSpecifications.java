package eu.transparency.lobbycal.web.rest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.Meeting_;
import eu.transparency.lobbycal.domain.Partner;
import eu.transparency.lobbycal.domain.Tag;

public final class MeetingSpecifications {
	private static final Logger log = LoggerFactory
			.getLogger(MeetingSpecifications.class);

	MeetingSpecifications() {
	}

	public static Specification<Meeting> hasTag(String searchTerm,
			Collection<Long> mepIds) {
		return new Specification<Meeting>() {
			@Override
			public Predicate toPredicate(Root<Meeting> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);
				String containsLikePattern = getContainsLikePattern(searchTerm);
				log.info(searchTerm + "," + mepIds);
				Root<Meeting> meeting = root;
				Subquery<Partner> partnerSubQuery = query.subquery(Partner.class);
				Root<Partner> partner = partnerSubQuery.from(Partner.class);
				Expression<Collection<Meeting>> partnerMeetings = partner.get("meetings");
				partnerSubQuery.select(partner);
				partnerSubQuery.where(cb.like(partner.get("name"), containsLikePattern),
						cb.isMember(meeting, partnerMeetings));
				return cb.exists(partnerSubQuery);
			}

		};

	}
	public static Specification<Meeting> past() {
		return new Specification<Meeting>() {
			@Override
			public Predicate toPredicate(Root<Meeting> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);
				return cb.lessThanOrEqualTo(root.<ZonedDateTime>get("startDate"),  ZonedDateTime.now());
			}

		};

	}
	
	
	public static Specification<Meeting> hasPartner(String searchTerm,
			Collection<Long> mepIds) {
		return new Specification<Meeting>() {
			@Override
			public Predicate toPredicate(Root<Meeting> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);
				String containsLikePattern = getContainsLikePattern(searchTerm);
				log.info(searchTerm + "," + mepIds);
				Root<Meeting> meeting = root;
				Subquery<Tag> tagSubQuery = query.subquery(Tag.class);
				Root<Tag> tag = tagSubQuery.from(Tag.class);
				Expression<Collection<Meeting>> tagMeetings = tag.get("meetings");
				tagSubQuery.select(tag);
				tagSubQuery.where(cb.like(tag.get("i18nKey"), containsLikePattern),
						cb.isMember(meeting, tagMeetings));
				return cb.exists(tagSubQuery);
			}

		};

	}

	public  static Specification<Meeting> hasTerm(String searchTerm,
			Collection<Long> mepIds) {
		return new Specification<Meeting>() {
			@Override
			public Predicate toPredicate(Root<Meeting> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);
				String containsLikePattern = getContainsLikePattern(searchTerm);
				log.info(searchTerm + "," + mepIds);
				List<Predicate> mustPreds = new ArrayList<>();
				mustPreds.add(root.join(Meeting_.user, JoinType.LEFT)
						.in(mepIds));

				try {
					// can criteria
					// @see CalendarDTRepositoryImpl
					cb.or(cb.like(cb.lower(root.<String> get("title")),
							containsLikePattern), cb.like(
							cb.lower(root.<String> get("mTag")),
							containsLikePattern), cb.like(
							cb.lower(root.<String> get("mPartner")),
							containsLikePattern));

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return andTogether(mustPreds, cb);
			}

		};

	}
	


	private static Predicate andTogether(List<Predicate> predicates,
			CriteriaBuilder cb) {
		return cb.and(predicates.toArray(new Predicate[0]));
	}

	private static Predicate orTogether(List<Predicate> predicates,
			CriteriaBuilder cb) {
		return cb.or(predicates.toArray(new Predicate[0]));
	}

	private static String getContainsLikePattern(String searchTerm) {
		if (searchTerm == null || searchTerm.isEmpty()) {
			return "%";
		} else {
			return "%" + searchTerm.toLowerCase() + "%";
		}
	}
}