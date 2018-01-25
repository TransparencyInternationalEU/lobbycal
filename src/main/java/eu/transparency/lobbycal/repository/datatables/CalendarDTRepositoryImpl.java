package eu.transparency.lobbycal.repository.datatables;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.parameter.ColumnParameter;
import org.springframework.data.jpa.datatables.parameter.OrderParameter;
import org.springframework.data.jpa.datatables.repository.DataTablesSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.transparency.lobbycal.web.rest.MeetingSpecifications;

@Transactional
public class CalendarDTRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements CalendarDTRepository<T, ID> {

	private static final Logger log = LoggerFactory.getLogger(CalendarDTRepositoryImpl.class);

	public CalendarDTRepositoryImpl(Class<T> domainClass, EntityManager em) {
		super(domainClass, em);
		log.info("");
	}

	
	
	public CalendarDTRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		log.info("");
	}

	@Override
	@Transactional(readOnly = true)
	public DataTablesOutput<T> findAll(DataTablesInput input) {

		log.info("");
		return findAll(input, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public DataTablesOutput<T> findAllForOne(DataTablesInput input, Specification<T> additionalSpecification,
			boolean future) {
		log.info("find all for one mep, future?:" +future);
		DataTablesOutput<T> output = new DataTablesOutput<T>();
		output.setDraw(input.getDraw());

		try {
			output.setRecordsTotal(count());

			Page<T> data = null;
			if (future) {

				data = findAll(
						Specifications.where(new DataTablesSpecification<T>(input)).and(additionalSpecification)
								.or((Specification<T>) MeetingSpecifications
										.hasPartner(input.getSearch().getValue().toLowerCase(), null)).and(additionalSpecification)
								.or((Specification<T>) MeetingSpecifications
										.hasTag(input.getSearch().getValue().toLowerCase(), null)).and(additionalSpecification)
								.or((Specification<T>) MeetingSpecifications
										.hasUserName(input.getSearch().getValue().toLowerCase(), null)).and(additionalSpecification),
						getPageable(input));

			} else {
				data = findAll(Specifications.where(new DataTablesSpecification<T>(input)).and(additionalSpecification)
						.and((Specification<T>) MeetingSpecifications.past())

						.or((Specification<T>) MeetingSpecifications
								.hasPartner(input.getSearch().getValue().toLowerCase(), null))
						.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

						.or((Specification<T>) MeetingSpecifications.hasTag(input.getSearch().getValue().toLowerCase(),
								null))
						.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

						.or((Specification<T>) MeetingSpecifications
								.hasUserName(input.getSearch().getValue().toLowerCase(), null))
						.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

						, getPageable(input));
			}
			output.setData(data.getContent());
			output.setRecordsFiltered(data.getTotalElements());

		} catch (Exception e) {
			output.setError(e.toString());
			output.setRecordsFiltered(0L);
		}

		return output;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public DataTablesOutput<T> findAll(DataTablesInput input, Specification<T> additionalSpecification) {

		DataTablesOutput<T> output = new DataTablesOutput<T>();
		output.setDraw(input.getDraw());

		try {
			output.setRecordsTotal(count());

			Page<T> data = findAll(Specifications.where(new DataTablesSpecification<T>(input))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

					.or((Specification<T>) MeetingSpecifications.hasPartner(input.getSearch().getValue().toLowerCase(),
							null))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

					.or((Specification<T>) MeetingSpecifications.hasTag(input.getSearch().getValue().toLowerCase(),
							null))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

					.or((Specification<T>) MeetingSpecifications.hasUserName(input.getSearch().getValue().toLowerCase(),
							null))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())

					, getPageable(input));

			output.setData(data.getContent());
			output.setRecordsFiltered(data.getTotalElements());

		} catch (Exception e) {
			output.setError(e.toString());
			output.setRecordsFiltered(0L);
		}

		return output;
	}

	/**
	 * Creates a 'LIMIT .. OFFSET .. ORDER BY ..' clause for the given
	 * {@link DataTablesInput}.
	 * 
	 * @param input
	 *            the {@link DataTablesInput} mapped from the Ajax request
	 * @return a {@link Pageable}, must not be {@literal null}.
	 */
	@Transactional
	private Pageable getPageable(DataTablesInput input) {

		List<Order> orders = new ArrayList<Order>();
		for (OrderParameter order : input.getOrder()) {
			log.trace("order column: " + order.getColumn() + "");
			ColumnParameter column = input.getColumns().get(order.getColumn());
			if (column.getOrderable()) {
				String sortColumn = column.getData();
				Direction sortDirection = Direction.fromString(order.getDir());
				orders.add(new Order(sortDirection, sortColumn));
			}
		}
		Sort sort = orders.isEmpty() ? null : new Sort(orders);
		if (input.getLength() == -1) {
			input.setStart(0);
			input.setLength(Integer.MAX_VALUE);
		}

		return new PageRequest(input.getStart() / input.getLength(), input.getLength(), sort);
	}
}
